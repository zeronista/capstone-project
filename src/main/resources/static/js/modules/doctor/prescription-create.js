/**
 * Doctor Prescription Creation Module
 * Handles medication management, autocomplete, AI suggestions, form validation, and prescription submission
 */

// ==================== STATE MANAGEMENT ====================
let currentSuggestion = null;
let autocompleteTimeout = null;
let activeAutocompleteField = null;

/**
 * Toggle mobile sidebar
 */
export function toggleMobileSidebar() {
    const sidebar = document.querySelector("aside");
    sidebar.classList.toggle("hidden");
}

// ==================== MEDICATION TABLE MANAGEMENT ====================

/**
 * Add medication row to the table
 */
export function addMedicationRow() {
    const tbody = document.getElementById("medicationTableBody");
    const rowCount = tbody.querySelectorAll(".medication-row").length + 1;

    const newRow = document.createElement("tr");
    newRow.className = "medication-row bg-white dark:bg-[#1a2634] hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors";
    newRow.innerHTML = `
        <td class="px-4 py-3 text-sm text-slate-600 dark:text-slate-400 row-number">${rowCount}</td>
        <td class="px-4 py-3 relative">
            <div class="relative">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[20px] pointer-events-none">search</span>
                <input type="text" name="medicationName[]" required
                       class="medication-name-input w-full pl-10 pr-4 py-2.5 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-colors text-sm"
                       placeholder="Tìm tên thuốc..."
                       oninput="window.prescriptionCreate.handleMedicationSearch(this)"
                       onfocus="window.prescriptionCreate.handleMedicationFocus(this)"
                       onblur="window.prescriptionCreate.handleMedicationBlur(this)" />
                <div class="autocomplete-dropdown hidden absolute top-full left-0 right-0 mt-1 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg shadow-lg z-10"></div>
            </div>
        </td>
        <td class="px-4 py-3">
            <input type="number" name="quantity[]" required min="1" value="1"
                   class="w-full px-3 py-2.5 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-colors text-sm text-center"
                   placeholder="1" />
        </td>
        <td class="px-4 py-3">
            <input type="text" name="instructions[]"
                   class="w-full px-3 py-2.5 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-colors text-sm"
                   placeholder="VD: Sáng 1 viên sau ăn" />
        </td>
        <td class="px-4 py-3 text-center">
            <button type="button" onclick="window.prescriptionCreate.removeMedicationRow(this)"
                    class="p-2 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors">
                <span class="material-symbols-outlined text-[20px]">delete</span>
            </button>
        </td>
    `;

    tbody.appendChild(newRow);
    updateRowNumbers();
}

/**
 * Remove medication row from the table
 */
export function removeMedicationRow(button) {
    const tbody = document.getElementById("medicationTableBody");
    const rows = tbody.querySelectorAll(".medication-row");

    if (rows.length > 1) {
        button.closest("tr").remove();
        updateRowNumbers();
    } else {
        showToast("Phải có ít nhất một loại thuốc trong đơn!", "error");
    }
}

/**
 * Update row numbers after add/remove
 */
function updateRowNumbers() {
    const rows = document.querySelectorAll(".medication-row");
    rows.forEach((row, index) => {
        row.querySelector(".row-number").textContent = index + 1;
    });
}

// ==================== AUTOCOMPLETE FUNCTIONALITY ====================

/**
 * Handle medication search input with debouncing
 */
export function handleMedicationSearch(input) {
    clearTimeout(autocompleteTimeout);
    const query = input.value.trim();

    if (query.length < 2) {
        hideAutocompleteDropdown(input);
        return;
    }

    autocompleteTimeout = setTimeout(() => {
        searchMedications(input, query);
    }, 300);
}

/**
 * Search medications via API
 */
async function searchMedications(input, query) {
    try {
        // Mock API call - replace with actual endpoint when available
        // const response = await fetch(`/api/medications/search?query=${encodeURIComponent(query)}`);
        // const data = await response.json();
        
        // Mock data for demonstration
        const mockMedications = [
            { id: 1, name: "Paracetamol 500mg", description: "Thuốc hạ sốt, giảm đau" },
            { id: 2, name: "Ibuprofen 400mg", description: "Thuốc chống viêm" },
            { id: 3, name: "Amoxicillin 500mg", description: "Kháng sinh" },
            { id: 4, name: "Vitamin C 1000mg", description: "Bổ sung vitamin" },
            { id: 5, name: "Omeprazole 20mg", description: "Thuốc dạ dày" }
        ].filter(med => med.name.toLowerCase().includes(query.toLowerCase()));

        showAutocompleteDropdown(input, mockMedications);
    } catch (error) {
        console.error("Error searching medications:", error);
        hideAutocompleteDropdown(input);
    }
}

/**
 * Show autocomplete dropdown with results
 */
function showAutocompleteDropdown(input, medications) {
    const dropdown = input.parentElement.querySelector('.autocomplete-dropdown');
    
    if (medications.length === 0) {
        dropdown.innerHTML = `
            <div class="px-4 py-3 text-sm text-slate-500 dark:text-slate-400">
                Không tìm thấy thuốc phù hợp
            </div>
        `;
    } else {
        dropdown.innerHTML = medications.map(med => `
            <div class="autocomplete-item px-4 py-2.5 cursor-pointer transition-colors text-sm" 
                 data-med-id="${med.id}" 
                 data-med-name="${med.name}"
                 onmousedown="window.prescriptionCreate.selectMedication(this, '${med.name}')">
                <div class="font-medium text-slate-900 dark:text-white">${med.name}</div>
                <div class="text-xs text-slate-500 dark:text-slate-400">${med.description}</div>
            </div>
        `).join('');
    }
    
    dropdown.classList.remove('hidden');
    activeAutocompleteField = input;
}

/**
 * Hide autocomplete dropdown
 */
function hideAutocompleteDropdown(input) {
    const dropdown = input.parentElement.querySelector('.autocomplete-dropdown');
    if (dropdown) {
        dropdown.classList.add('hidden');
    }
}

/**
 * Handle medication field focus
 */
export function handleMedicationFocus(input) {
    if (input.value.trim().length >= 2) {
        handleMedicationSearch(input);
    }
}

/**
 * Handle medication field blur with delay for click
 */
export function handleMedicationBlur(input) {
    setTimeout(() => {
        if (activeAutocompleteField === input) {
            hideAutocompleteDropdown(input);
            activeAutocompleteField = null;
        }
    }, 200);
}

/**
 * Select medication from autocomplete
 */
export function selectMedication(element, name) {
    if (activeAutocompleteField) {
        activeAutocompleteField.value = name;
        hideAutocompleteDropdown(activeAutocompleteField);
        activeAutocompleteField = null;
    }
}

// ==================== AI SUGGESTION FUNCTIONALITY ====================

/**
 * Load AI suggestions based on diagnosis
 */
async function loadAISuggestions(diagnosis) {
    if (!diagnosis || diagnosis.trim().length < 5) {
        return;
    }

    try {
        // Mock AI suggestion - replace with actual API when available
        // const response = await fetch(`/api/ai/prescription-suggestions?diagnosis=${encodeURIComponent(diagnosis)}`);
        // const data = await response.json();
        
        // Mock suggestion based on common conditions
        const mockSuggestion = generateMockSuggestion(diagnosis);
        
        if (mockSuggestion) {
            showAISuggestion(mockSuggestion);
        }
    } catch (error) {
        console.error("Error loading AI suggestions:", error);
    }
}

/**
 * Generate mock AI suggestion based on diagnosis keywords
 */
function generateMockSuggestion(diagnosis) {
    const diagnosisLower = diagnosis.toLowerCase();
    
    if (diagnosisLower.includes("đái tháo đường") || diagnosisLower.includes("tiểu đường")) {
        return {
            text: "Dựa trên phác đồ điều trị cho <strong>Đái tháo đường</strong>. Đề xuất thêm <strong>Metformin 500mg</strong> vào đơn thuốc để kiểm soát đường huyết.",
            medication: {
                name: "Metformin 500mg",
                quantity: 60,
                instructions: "Sáng 1 viên, tối 1 viên sau ăn"
            }
        };
    } else if (diagnosisLower.includes("huyết áp") || diagnosisLower.includes("cao huyết áp")) {
        return {
            text: "Dựa trên phác đồ điều trị cho <strong>Cao huyết áp</strong>. Đề xuất thêm <strong>Amlodipine 5mg</strong> vào đơn thuốc để kiểm soát huyết áp.",
            medication: {
                name: "Amlodipine 5mg",
                quantity: 30,
                instructions: "Sáng 1 viên trước ăn"
            }
        };
    } else if (diagnosisLower.includes("ho") || diagnosisLower.includes("cảm")) {
        return {
            text: "Dựa trên triệu chứng <strong>Ho và cảm cúm</strong>. Đề xuất thêm <strong>Vitamin C 1000mg</strong> vào đơn thuốc để tăng cường miễn dịch.",
            medication: {
                name: "Vitamin C 1000mg",
                quantity: 30,
                instructions: "Ngày 1 viên sau ăn sáng"
            }
        };
    }
    
    return null;
}

/**
 * Show AI suggestion box
 */
function showAISuggestion(suggestion) {
    currentSuggestion = suggestion;
    const box = document.getElementById("aiSuggestionBox");
    const textElement = document.getElementById("aiSuggestionText");
    
    textElement.innerHTML = suggestion.text;
    box.classList.remove("hidden");
}

/**
 * Apply AI suggestion to medication table
 */
export function applySuggestion() {
    if (!currentSuggestion || !currentSuggestion.medication) {
        showToast("Không có gợi ý để áp dụng", "error");
        return;
    }

    // Add a new medication row with the suggestion
    addMedicationRow();
    
    // Get the last row (newly added)
    const tbody = document.getElementById("medicationTableBody");
    const rows = tbody.querySelectorAll(".medication-row");
    const lastRow = rows[rows.length - 1];
    
    // Fill in the suggestion data
    const nameInput = lastRow.querySelector('input[name="medicationName[]"]');
    const quantityInput = lastRow.querySelector('input[name="quantity[]"]');
    const instructionsInput = lastRow.querySelector('input[name="instructions[]"]');
    
    nameInput.value = currentSuggestion.medication.name;
    quantityInput.value = currentSuggestion.medication.quantity;
    instructionsInput.value = currentSuggestion.medication.instructions;
    
    // Hide the suggestion box
    document.getElementById("aiSuggestionBox").classList.add("hidden");
    currentSuggestion = null;
    
    showToast("Đã áp dụng gợi ý từ AI", "success");
}

// ==================== VALIDATION ====================

/**
 * Clear all validation errors
 */
function clearAllErrors() {
    document.querySelectorAll(".field-error").forEach((el) => el.remove());
    document.querySelectorAll(".border-red-500").forEach((el) => {
        el.classList.remove("border-red-500", "focus:ring-red-500", "focus:border-red-500");
        el.classList.add("border-slate-300", "dark:border-slate-700");
    });
}

/**
 * Show field-level error
 */
function showFieldError(fieldName, message) {
    const field = document.querySelector(`[name="${fieldName}"]`) || document.getElementById(fieldName);
    if (!field) return;

    clearFieldError(fieldName);

    field.classList.add("border-red-500", "focus:ring-red-500", "focus:border-red-500");
    field.classList.remove("border-slate-300", "dark:border-slate-700");

    const errorDiv = document.createElement("div");
    errorDiv.className = "field-error text-red-600 dark:text-red-400 text-xs mt-1 flex items-start gap-1";
    errorDiv.innerHTML = `
        <span class="material-symbols-outlined text-sm">error</span>
        <span>${message}</span>
    `;
    field.parentElement.appendChild(errorDiv);
}

/**
 * Clear field-level error
 */
function clearFieldError(fieldName) {
    const field = document.querySelector(`[name="${fieldName}"]`) || document.getElementById(fieldName);
    if (!field) return;

    field.classList.remove("border-red-500", "focus:ring-red-500", "focus:border-red-500");
    field.classList.add("border-slate-300", "dark:border-slate-700");

    const errorDiv = field.parentElement.querySelector(".field-error");
    if (errorDiv) {
        errorDiv.remove();
    }
}

/**
 * Validate diagnosis field
 */
function validateDiagnosis(diagnosis) {
    if (!diagnosis || diagnosis.trim().length < 5) {
        return "Chẩn đoán phải có ít nhất 5 ký tự";
    }
    if (diagnosis.length > 500) {
        return "Chẩn đoán không được vượt quá 500 ký tự";
    }
    return null;
}

/**
 * Validate medication object
 */
function validateMedication(med) {
    const errors = [];

    if (!med.name || med.name.trim().length < 2) {
        errors.push("Tên thuốc phải có ít nhất 2 ký tự");
    }
    if (med.name && med.name.length > 200) {
        errors.push("Tên thuốc không được vượt quá 200 ký tự");
    }
    if (!med.quantity || med.quantity < 1 || med.quantity > 1000) {
        errors.push("Số lượng phải từ 1 đến 1000");
    }
    if (med.instructions && med.instructions.length > 500) {
        errors.push("Hướng dẫn sử dụng không được vượt quá 500 ký tự");
    }

    return errors.length > 0 ? errors.join(", ") : null;
}

/**
 * Validate entire form data
 */
function validateForm(data) {
    const errors = {};

    // Validate diagnosis
    const diagnosisError = validateDiagnosis(data.diagnosis);
    if (diagnosisError) {
        errors.diagnosis = [diagnosisError];
    }

    // Validate medications
    if (!data.medications || data.medications.length === 0) {
        errors.medications = ["Phải có ít nhất một loại thuốc"];
    } else {
        data.medications.forEach((med, index) => {
            const medError = validateMedication(med);
            if (medError) {
                errors[`medication_${index}`] = [medError];
            }
        });
    }

    // Validate revisit date if provided
    if (data.revisitDate) {
        const revisitDateObj = new Date(data.revisitDate);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (revisitDateObj <= today) {
            errors.revisitDate = ["Ngày tái khám phải là ngày trong tương lai"];
        }
    }

    return errors;
}

/**
 * Display validation errors in UI
 */
function displayValidationErrors(errors) {
    clearAllErrors();

    for (const [field, messages] of Object.entries(errors)) {
        const message = Array.isArray(messages) ? messages.join(", ") : messages;

        if (field.startsWith("medication_")) {
            const index = parseInt(field.split("_")[1]);
            const row = document.querySelectorAll(".medication-row")[index];
            if (row) {
                const nameField = row.querySelector('input[name="medicationName[]"]');
                if (nameField) {
                    nameField.classList.add("border-red-500");
                    const errorDiv = document.createElement("div");
                    errorDiv.className = "field-error text-red-600 dark:text-red-400 text-xs mt-1";
                    errorDiv.textContent = message;
                    nameField.parentElement.appendChild(errorDiv);
                }
            }
        } else if (field === "medications") {
            const table = document.getElementById("medicationTable");
            const existingError = table.previousElementSibling;
            if (!existingError || !existingError.classList.contains("medication-table-error")) {
                const errorDiv = document.createElement("div");
                errorDiv.className = "medication-table-error bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg mb-4 flex items-center gap-2";
                errorDiv.innerHTML = `
                    <span class="material-symbols-outlined">error</span>
                    <span>${message}</span>
                `;
                table.parentElement.insertBefore(errorDiv, table);
            }
        } else {
            showFieldError(field, message);
        }
    }

    const firstError = document.querySelector(".border-red-500, .medication-table-error");
    if (firstError) {
        firstError.scrollIntoView({ behavior: "smooth", block: "center" });
    }

    showToast("Vui lòng kiểm tra lại các trường đã nhập", "error");
}

// ==================== TOAST NOTIFICATIONS ====================

/**
 * Show toast notification
 */
function showToast(message, type = "info") {
    const container = document.getElementById("toastContainer");
    
    const toast = document.createElement("div");
    toast.className = `toast-enter flex items-center gap-3 px-6 py-3 rounded-lg shadow-lg text-white ${
        type === "error" ? "bg-red-500" : type === "success" ? "bg-green-500" : "bg-blue-500"
    }`;
    
    const icon = type === "error" ? "error" : type === "success" ? "check_circle" : "info";
    toast.innerHTML = `
        <span class="material-symbols-outlined text-[20px]">${icon}</span>
        <span class="font-medium">${message}</span>
    `;
    
    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add("toast-exit");
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ==================== FORM SUBMISSION ====================

/**
 * Setup form submission handler
 */
function setupFormSubmission() {
    const form = document.getElementById("prescriptionForm");
    if (!form) return;

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        clearAllErrors();
        document.querySelectorAll(".medication-table-error").forEach((el) => el.remove());

        const formData = new FormData(form);
        const urlParams = new URLSearchParams(window.location.search);
        const patientId = urlParams.get("patientId");

        // Get doctor ID from hidden field
        const doctorIdElement = document.getElementById("doctorId");
        const doctorId = doctorIdElement ? parseInt(doctorIdElement.value) : null;

        const data = {
            patientId: patientId ? parseInt(patientId) : null,
            doctorId: doctorId,
            diagnosis: formData.get("diagnosis"),
            symptoms: formData.get("symptoms") || "",
            notes: formData.get("notes") || "",
            revisitDate: formData.get("revisitDate") || null,
            medications: [],
        };

        // Collect medications
        const medicationNames = formData.getAll("medicationName[]");
        const quantities = formData.getAll("quantity[]");
        const instructions = formData.getAll("instructions[]");

        for (let i = 0; i < medicationNames.length; i++) {
            if (medicationNames[i].trim()) {
                data.medications.push({
                    name: medicationNames[i].trim(),
                    quantity: parseInt(quantities[i]) || 1,
                    dosage: "N/A", // For backward compatibility
                    instructions: instructions[i].trim() || "",
                });
            }
        }

        // Client-side validation
        const validationErrors = validateForm(data);
        if (Object.keys(validationErrors).length > 0) {
            displayValidationErrors(validationErrors);
            return;
        }

        // Disable submit button
        const submitButton = form.querySelector('button[type="submit"]');
        const originalButtonHTML = submitButton.innerHTML;
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="material-symbols-outlined animate-spin">progress_activity</span><span>Đang lưu...</span>';

        try {
            const response = await fetch("/api/prescriptions/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data),
            });

            if (response.ok) {
                const result = await response.json();
                showToast("Đơn thuốc đã được lưu thành công!", "success");

                setTimeout(() => {
                    if (patientId) {
                        window.location.href = `/doctor/patients?id=${patientId}`;
                    } else {
                        window.location.href = `/doctor/prescriptions`;
                    }
                }, 1500);
            } else {
                const errorData = await response.json();

                if (errorData.errors && Object.keys(errorData.errors).length > 0) {
                    displayValidationErrors(errorData.errors);
                } else {
                    showToast(errorData.message || "Có lỗi xảy ra khi lưu đơn thuốc", "error");
                }
            }
        } catch (error) {
            console.error("Error submitting prescription:", error);
            showToast("Không thể kết nối đến máy chủ. Vui lòng thử lại.", "error");
        } finally {
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonHTML;
        }
    });
}

/**
 * Setup diagnosis AI listener
 */
function setupDiagnosisAIListener() {
    const diagnosisInput = document.getElementById("mainDiagnosis");
    if (!diagnosisInput) return;

    let diagnosisTimeout;
    diagnosisInput.addEventListener("input", function() {
        clearTimeout(diagnosisTimeout);
        diagnosisTimeout = setTimeout(() => {
            loadAISuggestions(this.value);
        }, 1000);
    });
}

/**
 * Setup follow-up date minimum
 */
function setupFollowUpDate() {
    const followUpInput = document.getElementById("followUpDate");
    if (!followUpInput) return;

    // Set minimum date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const minDate = tomorrow.toISOString().split('T')[0];
    followUpInput.setAttribute('min', minDate);
}

// ==================== INITIALIZATION ====================

/**
 * Initialize the prescription creation module
 */
export function init() {
    console.log("Initializing prescription creation module");
    
    // Initialize with 2 empty medication rows
    addMedicationRow();
    addMedicationRow();
    
    // Setup form handlers
    setupFormSubmission();
    setupDiagnosisAIListener();
    setupFollowUpDate();
    
    console.log("Prescription creation module initialized");
}
