/**
 * Doctor Prescription Creation Module
 * Handles medication management, form validation, and prescription submission
 */

/**
 * Toggle mobile sidebar
 */
export function toggleMobileSidebar() {
    const sidebar = document.querySelector("aside");
    sidebar.classList.toggle("hidden");
}

/**
 * Add medication row to the table
 */
export function addMedicationRow() {
    const tbody = document.getElementById("medicationTableBody");
    const rowCount = tbody.querySelectorAll(".medication-row").length + 1;

    const newRow = document.createElement("tr");
    newRow.className = "medication-row hover:bg-slate-50";
    newRow.innerHTML = `
        <td class="px-4 py-3 text-sm text-slate-600 row-number">${rowCount}</td>
        <td class="px-4 py-3">
            <input type="text" name="medicationName[]" required
                   class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
                   placeholder="Tên thuốc..." />
        </td>
        <td class="px-4 py-3">
            <input type="number" name="quantity[]" required min="1" value="1"
                   class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
                   placeholder="SL" />
        </td>
        <td class="px-4 py-3">
            <input type="text" name="dosage[]" required
                   class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
                   placeholder="Ví dụ: 500mg" />
        </td>
        <td class="px-4 py-3">
            <input type="text" name="instructions[]"
                   class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
                   placeholder="Ngày 2 lần, sau ăn..." />
        </td>
        <td class="px-4 py-3 text-center">
            <button type="button" onclick="window.prescriptionCreate.removeMedicationRow(this)"
                    class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                <span class="material-symbols-outlined text-lg">delete</span>
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
        alert("Phải có ít nhất một loại thuốc trong đơn!");
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

/**
 * Setup revisit date toggle
 */
function setupRevisitToggle() {
    const revisitCheckbox = document.querySelector('input[name="requireRevisit"]');
    const revisitDateField = document.getElementById("revisitDateField");

    if (revisitCheckbox) {
        revisitCheckbox.addEventListener("change", function () {
            if (this.checked) {
                revisitDateField.classList.remove("hidden");
                revisitDateField.querySelector("input").required = true;
            } else {
                revisitDateField.classList.add("hidden");
                revisitDateField.querySelector("input").required = false;
            }
        });
    }
}

/**
 * Show field-level error
 */
function showFieldError(fieldName, message) {
    const field = document.querySelector(`[name="${fieldName}"]`);
    if (!field) return;

    clearFieldError(fieldName);

    field.classList.add("border-red-500", "focus:ring-red-500", "focus:border-red-500");
    field.classList.remove("border-slate-300");

    const errorDiv = document.createElement("div");
    errorDiv.className = "field-error text-red-600 text-xs mt-1 flex items-start gap-1";
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
    const field = document.querySelector(`[name="${fieldName}"]`);
    if (!field) return;

    field.classList.remove("border-red-500", "focus:ring-red-500", "focus:border-red-500");
    field.classList.add("border-slate-300");

    const errorDiv = field.parentElement.querySelector(".field-error");
    if (errorDiv) {
        errorDiv.remove();
    }
}

/**
 * Clear all validation errors
 */
function clearAllErrors() {
    document.querySelectorAll(".field-error").forEach((el) => el.remove());
    document.querySelectorAll(".border-red-500").forEach((el) => {
        el.classList.remove("border-red-500", "focus:ring-red-500", "focus:border-red-500");
        el.classList.add("border-slate-300");
    });
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
    if (!med.dosage || med.dosage.trim().length === 0) {
        errors.push("Liều dùng không được để trống");
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

    // Validate revisit date
    if (data.requireRevisit && !data.revisitDate) {
        errors.revisitDate = ["Vui lòng chọn ngày tái khám"];
    }
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
                    errorDiv.className = "field-error text-red-600 text-xs mt-1";
                    errorDiv.textContent = message;
                    nameField.parentElement.appendChild(errorDiv);
                }
            }
        } else if (field === "medications") {
            const table = document.getElementById("medicationTable");
            const existingError = table.previousElementSibling;
            if (!existingError || !existingError.classList.contains("medication-table-error")) {
                const errorDiv = document.createElement("div");
                errorDiv.className = "medication-table-error bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4";
                errorDiv.textContent = message;
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

/**
 * Show toast notification
 */
function showToast(message, type = "info") {
    const toast = document.createElement("div");
    toast.className = `fixed top-4 right-4 px-6 py-3 rounded-lg shadow-lg text-white z-50 animate-slide-in ${
        type === "error" ? "bg-red-500" : type === "success" ? "bg-green-500" : "bg-blue-500"
    }`;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add("animate-fade-out");
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

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

        // Get doctor ID from Thymeleaf model
        const doctorIdElement = document.getElementById("doctorId");
        const doctorId = doctorIdElement ? parseInt(doctorIdElement.value) : null;

        const data = {
            patientId: patientId ? parseInt(patientId) : null,
            doctorId: doctorId,
            diagnosis: formData.get("diagnosis"),
            notes: formData.get("notes") || "",
            requireRevisit: formData.get("requireRevisit") === "on",
            revisitDate: formData.get("revisitDate") || null,
            medications: [],
        };

        // Collect medications
        const medicationNames = formData.getAll("medicationName[]");
        const quantities = formData.getAll("quantity[]");
        const dosages = formData.getAll("dosage[]");
        const instructions = formData.getAll("instructions[]");

        for (let i = 0; i < medicationNames.length; i++) {
            if (medicationNames[i].trim()) {
                data.medications.push({
                    name: medicationNames[i].trim(),
                    quantity: parseInt(quantities[i]) || 1,
                    dosage: dosages[i].trim(),
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
                    window.location.href = `/doctor/patients?id=${patientId}`;
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
 * Initialize the prescription creation module
 */
export function init() {
    setupRevisitToggle();
    setupFormSubmission();
}
