// Patient Portal JavaScript
let currentTab = 'prescriptions';
let prescriptionsData = [];
let treatmentsData = [];
let ticketsData = [];
let documentsData = [];
let profileData = null;
let deleteDocumentId = null;

// Switch Tab Function
function switchTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.add('hidden');
    });
    
    // Remove active class from all buttons
    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected tab
    document.getElementById('content-' + tabName).classList.remove('hidden');
    document.getElementById('tab-' + tabName).classList.add('active');
    
    currentTab = tabName;
    
    // Load data if not loaded yet
    if (tabName === 'prescriptions' && prescriptionsData.length === 0) {
        loadPrescriptions();
    } else if (tabName === 'treatments' && treatmentsData.length === 0) {
        loadTreatments();
    } else if (tabName === 'tickets' && ticketsData.length === 0) {
        loadTickets();
    } else if (tabName === 'documents' && documentsData.length === 0) {
        loadDocuments();
    } else if (tabName === 'profile' && !profileData) {
        loadProfile();
    }
}

// Load Statistics
async function loadStats() {
    try {
        const response = await fetch('/api/patient/stats');
        const data = await response.json();
        
        if (data.success) {
            document.getElementById('statPrescriptions').textContent = data.stats.prescriptions;
            document.getElementById('statTreatments').textContent = data.stats.treatments;
            document.getElementById('statTickets').textContent = data.stats.tickets;
            document.getElementById('statOpenTickets').textContent = data.stats.openTickets;
            document.getElementById('statDocuments').textContent = data.stats.documents || 0;
        }
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

// Load Prescriptions
async function loadPrescriptions() {
    try {
        const response = await fetch('/api/patient/prescriptions');
        const data = await response.json();
        
        if (data.success) {
            prescriptionsData = data.prescriptions;
            renderPrescriptions();
        } else {
            showError('prescriptionsList', data.message);
        }
    } catch (error) {
        console.error('Error loading prescriptions:', error);
        showError('prescriptionsList', 'Không thể tải danh sách đơn thuốc');
    }
}

// Render Prescriptions
function renderPrescriptions() {
    const container = document.getElementById('prescriptionsList');
    
    if (prescriptionsData.length === 0) {
        container.innerHTML = `
            <div class="text-center py-12">
                <i class="fas fa-prescription text-6xl text-gray-300 mb-4"></i>
                <p class="text-gray-500 text-lg">Chưa có đơn thuốc nào</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = prescriptionsData.map(prescription => `
        <div class="border border-gray-200 rounded-lg p-6 hover:shadow-lg transition-shadow">
            <div class="flex items-start justify-between">
                <div class="flex-1">
                    <div class="flex items-center gap-3 mb-3">
                        <span class="px-3 py-1 rounded-full text-sm font-medium ${getStatusBadge(prescription.status)}">
                            ${getStatusText(prescription.status)}
                        </span>
                        <span class="text-sm text-gray-500">
                            <i class="fas fa-calendar-alt mr-1"></i>${formatDate(prescription.prescriptionDate)}
                        </span>
                    </div>
                    <h4 class="text-lg font-semibold text-gray-800 mb-2">Chẩn đoán: ${prescription.diagnosis || 'Không có'}</h4>
                    <p class="text-gray-600 text-sm mb-3">
                        <i class="fas fa-user-md mr-2 text-blue-500"></i>Bác sĩ: <span class="font-medium">${prescription.doctor?.userInfo?.fullName || 'N/A'}</span>
                    </p>
                    ${prescription.notes ? `<p class="text-gray-600 text-sm"><i class="fas fa-sticky-note mr-2 text-yellow-500"></i>${prescription.notes}</p>` : ''}
                </div>
                <button onclick="viewPrescriptionDetail(${prescription.id})" class="ml-4 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors">
                    <i class="fas fa-eye mr-2"></i>Xem chi tiết
                </button>
            </div>
        </div>
    `).join('');
}

// Load Treatments
async function loadTreatments() {
    try {
        const response = await fetch('/api/patient/treatments');
        const data = await response.json();
        
        if (data.success) {
            treatmentsData = data.treatments;
            renderTreatments();
        } else {
            showError('treatmentsList', data.message);
        }
    } catch (error) {
        console.error('Error loading treatments:', error);
        showError('treatmentsList', 'Không thể tải danh sách kế hoạch điều trị');
    }
}

// Render Treatments
function renderTreatments() {
    const container = document.getElementById('treatmentsList');
    
    if (treatmentsData.length === 0) {
        container.innerHTML = `
            <div class="text-center py-12">
                <i class="fas fa-notes-medical text-6xl text-gray-300 mb-4"></i>
                <p class="text-gray-500 text-lg">Chưa có kế hoạch điều trị nào</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = treatmentsData.map(treatment => `
        <div class="border border-gray-200 rounded-lg p-6 hover:shadow-lg transition-shadow">
            <div class="flex items-start justify-between">
                <div class="flex-1">
                    <div class="flex items-center gap-3 mb-3">
                        <span class="px-3 py-1 rounded-full text-sm font-medium ${getTreatmentStatusBadge(treatment.status)}">
                            ${getTreatmentStatusText(treatment.status)}
                        </span>
                        ${treatment.aiSuggested ? '<span class="px-3 py-1 bg-purple-100 text-purple-800 rounded-full text-sm font-medium"><i class="fas fa-robot mr-1"></i>AI Gợi ý</span>' : ''}
                    </div>
                    <h4 class="text-lg font-semibold text-gray-800 mb-2">${treatment.planName || 'Kế hoạch điều trị'}</h4>
                    <p class="text-gray-600 text-sm mb-2">
                        <i class="fas fa-user-md mr-2 text-blue-500"></i>Bác sĩ: <span class="font-medium">${treatment.doctor?.userInfo?.fullName || 'N/A'}</span>
                    </p>
                    <div class="grid grid-cols-2 gap-2 text-sm text-gray-600">
                        <p><i class="fas fa-calendar-alt mr-2 text-green-500"></i>Bắt đầu: ${formatDate(treatment.startDate)}</p>
                        <p><i class="fas fa-calendar-check mr-2 text-orange-500"></i>Dự kiến kết thúc: ${formatDate(treatment.expectedEndDate)}</p>
                    </div>
                    ${treatment.notes ? `<p class="text-gray-600 text-sm mt-2"><i class="fas fa-sticky-note mr-2 text-yellow-500"></i>${treatment.notes}</p>` : ''}
                </div>
            </div>
        </div>
    `).join('');
}

// Load Tickets
async function loadTickets() {
    try {
        const response = await fetch('/api/patient/tickets');
        const data = await response.json();
        
        if (data.success) {
            ticketsData = data.tickets;
            renderTickets();
        } else {
            showError('ticketsList', data.message);
        }
    } catch (error) {
        console.error('Error loading tickets:', error);
        showError('ticketsList', 'Không thể tải danh sách yêu cầu hỗ trợ');
    }
}

// Render Tickets
function renderTickets() {
    const container = document.getElementById('ticketsList');
    
    if (ticketsData.length === 0) {
        container.innerHTML = `
            <div class="text-center py-12">
                <i class="fas fa-ticket-alt text-6xl text-gray-300 mb-4"></i>
                <p class="text-gray-500 text-lg">Chưa có yêu cầu hỗ trợ nào</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = ticketsData.map(ticket => `
        <div class="border border-gray-200 rounded-lg p-6 hover:shadow-lg transition-shadow">
            <div class="flex items-start justify-between">
                <div class="flex-1">
                    <div class="flex items-center gap-3 mb-3">
                        <span class="px-3 py-1 rounded-full text-sm font-medium ${getTicketStatusBadge(ticket.status)}">
                            ${getTicketStatusText(ticket.status)}
                        </span>
                        <span class="px-3 py-1 rounded-full text-sm font-medium ${getPriorityBadge(ticket.priority)}">
                            ${getPriorityText(ticket.priority)}
                        </span>
                    </div>
                    <h4 class="text-lg font-semibold text-gray-800 mb-2">${ticket.title}</h4>
                    <p class="text-gray-600 text-sm mb-3">${ticket.description || ''}</p>
                    <div class="flex items-center gap-4 text-sm text-gray-500">
                        <span><i class="fas fa-calendar-alt mr-2"></i>${formatDateTime(ticket.createdAt)}</span>
                        ${ticket.assignedTo ? `<span><i class="fas fa-user-md mr-2"></i>Phụ trách: ${ticket.assignedTo?.userInfo?.fullName || 'N/A'}</span>` : ''}
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// ========== DOCUMENT FUNCTIONS ==========

// Load Documents
async function loadDocuments() {
    try {
        const response = await fetch('/api/patient/documents');
        const data = await response.json();
        
        if (data.success) {
            documentsData = data.documents;
            renderDocuments();
        } else {
            showError('documentsList', data.message);
        }
    } catch (error) {
        console.error('Error loading documents:', error);
        showError('documentsList', 'Không thể tải danh sách tài liệu');
    }
}

// Render Documents
function renderDocuments() {
    const container = document.getElementById('documentsList');
    const filter = document.getElementById('documentTypeFilter')?.value || '';
    
    let filteredDocs = documentsData;
    if (filter) {
        filteredDocs = documentsData.filter(doc => doc.documentType === filter);
    }
    
    if (filteredDocs.length === 0) {
        container.innerHTML = `
            <div class="text-center py-12">
                <i class="fas fa-folder-open text-6xl text-gray-300 mb-4"></i>
                <p class="text-gray-500 text-lg">${filter ? 'Không có tài liệu nào thuộc loại này' : 'Chưa có tài liệu nào'}</p>
                <button onclick="openUploadModal()" class="mt-4 px-4 py-2 bg-cyan-500 text-white rounded-lg hover:bg-cyan-600 transition-colors">
                    <i class="fas fa-upload mr-2"></i>Upload tài liệu đầu tiên
                </button>
            </div>
        `;
        return;
    }
    
    container.innerHTML = filteredDocs.map(doc => `
        <div class="border border-gray-200 rounded-lg p-6 hover:shadow-lg transition-shadow">
            <div class="flex items-start justify-between">
                <div class="flex items-start gap-4 flex-1">
                    <div class="bg-cyan-100 p-3 rounded-lg">
                        <i class="${getFileIcon(doc.fileName)} text-2xl text-cyan-600"></i>
                    </div>
                    <div class="flex-1">
                        <div class="flex items-center gap-3 mb-2">
                            <span class="px-3 py-1 rounded-full text-xs font-medium ${getDocTypeBadge(doc.documentType)}">
                                ${doc.documentTypeLabel}
                            </span>
                        </div>
                        <h4 class="text-lg font-semibold text-gray-800 mb-1">${doc.fileName}</h4>
                        ${doc.description ? `<p class="text-gray-600 text-sm mb-2">${doc.description}</p>` : ''}
                        <div class="flex items-center gap-4 text-sm text-gray-500">
                            <span><i class="fas fa-calendar-alt mr-1"></i>${formatDateTime(doc.uploadDate)}</span>
                            <span><i class="fas fa-file mr-1"></i>${doc.fileSizeFormatted}</span>
                        </div>
                    </div>
                </div>
                <div class="flex items-center gap-2 ml-4">
                    ${doc.viewUrl ? `
                        <a href="${doc.viewUrl}" target="_blank" class="px-3 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors" title="Xem tài liệu">
                            <i class="fas fa-eye"></i>
                        </a>
                        <a href="${doc.viewUrl}" download class="px-3 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors" title="Tải xuống">
                            <i class="fas fa-download"></i>
                        </a>
                    ` : ''}
                    <button onclick="openDeleteModal(${doc.id}, '${escapeHtml(doc.fileName)}')" class="px-3 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors" title="Xóa">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

// Filter documents by type
function filterDocuments() {
    renderDocuments();
}

// Upload Modal Functions
function openUploadModal() {
    document.getElementById('uploadModal').classList.remove('hidden');
    document.getElementById('uploadModal').classList.add('flex');
    // Reset form
    document.getElementById('uploadForm').reset();
    document.getElementById('filePreview').innerHTML = `
        <i class="fas fa-cloud-upload-alt text-4xl text-gray-400 mb-2"></i>
        <p class="text-gray-600">Click để chọn file hoặc kéo thả vào đây</p>
        <p class="text-sm text-gray-400 mt-1">PDF, Word, Excel, Ảnh (max 10MB)</p>
    `;
    document.getElementById('uploadProgress').classList.add('hidden');
}

function closeUploadModal() {
    document.getElementById('uploadModal').classList.add('hidden');
    document.getElementById('uploadModal').classList.remove('flex');
}

function updateFilePreview() {
    const fileInput = document.getElementById('uploadFile');
    const preview = document.getElementById('filePreview');
    
    if (fileInput.files && fileInput.files[0]) {
        const file = fileInput.files[0];
        const fileSize = formatFileSizeJS(file.size);
        preview.innerHTML = `
            <i class="${getFileIcon(file.name)} text-4xl text-cyan-600 mb-2"></i>
            <p class="text-gray-800 font-medium">${file.name}</p>
            <p class="text-sm text-gray-500">${fileSize}</p>
        `;
    }
}

// Handle Upload
async function handleUpload(event) {
    event.preventDefault();
    
    const fileInput = document.getElementById('uploadFile');
    const documentType = document.getElementById('uploadDocumentType').value;
    const description = document.getElementById('uploadDescription').value;
    
    if (!fileInput.files || !fileInput.files[0]) {
        alert('Vui lòng chọn file');
        return;
    }
    
    if (!documentType) {
        alert('Vui lòng chọn loại tài liệu');
        return;
    }
    
    const file = fileInput.files[0];
    
    // Check file size (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
        alert('File không được vượt quá 10MB');
        return;
    }
    
    // Show progress
    document.getElementById('uploadProgress').classList.remove('hidden');
    document.getElementById('uploadButton').disabled = true;
    
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);
    if (description) {
        formData.append('description', description);
    }
    
    try {
        // Simulate progress (since fetch doesn't have native progress)
        let progress = 0;
        const progressInterval = setInterval(() => {
            progress += 10;
            if (progress <= 90) {
                document.getElementById('uploadProgressBar').style.width = progress + '%';
                document.getElementById('uploadPercent').textContent = progress + '%';
            }
        }, 200);
        
        const response = await fetch('/api/patient/documents/upload', {
            method: 'POST',
            body: formData
        });
        
        clearInterval(progressInterval);
        document.getElementById('uploadProgressBar').style.width = '100%';
        document.getElementById('uploadPercent').textContent = '100%';
        
        const data = await response.json();
        
        if (data.success) {
            closeUploadModal();
            // Reload documents
            documentsData = [];
            await loadDocuments();
            await loadStats();
            showToast('Upload tài liệu thành công!', 'success');
        } else {
            alert(data.message || 'Lỗi upload file');
        }
    } catch (error) {
        console.error('Error uploading document:', error);
        alert('Lỗi upload file: ' + error.message);
    } finally {
        document.getElementById('uploadButton').disabled = false;
        document.getElementById('uploadProgress').classList.add('hidden');
        document.getElementById('uploadProgressBar').style.width = '0%';
        document.getElementById('uploadPercent').textContent = '0%';
    }
}

// Delete Modal Functions
function openDeleteModal(docId, docName) {
    deleteDocumentId = docId;
    document.getElementById('deleteDocName').textContent = docName;
    document.getElementById('deleteModal').classList.remove('hidden');
    document.getElementById('deleteModal').classList.add('flex');
}

function closeDeleteModal() {
    document.getElementById('deleteModal').classList.add('hidden');
    document.getElementById('deleteModal').classList.remove('flex');
    deleteDocumentId = null;
}

async function confirmDelete() {
    if (!deleteDocumentId) return;
    
    document.getElementById('confirmDeleteBtn').disabled = true;
    
    try {
        const response = await fetch(`/api/patient/documents/${deleteDocumentId}`, {
            method: 'DELETE'
        });
        
        const data = await response.json();
        
        if (data.success) {
            closeDeleteModal();
            // Remove from local data and re-render
            documentsData = documentsData.filter(doc => doc.id !== deleteDocumentId);
            renderDocuments();
            await loadStats();
            showToast('Xóa tài liệu thành công!', 'success');
        } else {
            alert(data.message || 'Lỗi xóa tài liệu');
        }
    } catch (error) {
        console.error('Error deleting document:', error);
        alert('Lỗi xóa tài liệu: ' + error.message);
    } finally {
        document.getElementById('confirmDeleteBtn').disabled = false;
    }
}

// Helper functions for documents
function getFileIcon(filename) {
    if (!filename) return 'fas fa-file';
    const ext = filename.split('.').pop().toLowerCase();
    const icons = {
        'pdf': 'fas fa-file-pdf',
        'doc': 'fas fa-file-word',
        'docx': 'fas fa-file-word',
        'xls': 'fas fa-file-excel',
        'xlsx': 'fas fa-file-excel',
        'jpg': 'fas fa-file-image',
        'jpeg': 'fas fa-file-image',
        'png': 'fas fa-file-image',
        'gif': 'fas fa-file-image',
        'txt': 'fas fa-file-alt'
    };
    return icons[ext] || 'fas fa-file';
}

function getDocTypeBadge(type) {
    const badges = {
        'MEDICAL_HISTORY': 'bg-blue-100 text-blue-800',
        'PRESCRIPTION': 'bg-green-100 text-green-800',
        'TEST_RESULT': 'bg-purple-100 text-purple-800',
        'OTHER': 'bg-gray-100 text-gray-800'
    };
    return badges[type] || 'bg-gray-100 text-gray-800';
}

function formatFileSizeJS(bytes) {
    if (!bytes) return 'N/A';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML.replace(/'/g, "\\'");
}

function showToast(message, type = 'info') {
    // Simple toast notification
    const toast = document.createElement('div');
    toast.className = `fixed bottom-4 right-4 px-6 py-3 rounded-lg text-white shadow-lg z-50 transition-opacity duration-300 ${
        type === 'success' ? 'bg-green-500' : type === 'error' ? 'bg-red-500' : 'bg-blue-500'
    }`;
    toast.innerHTML = `<i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-times-circle' : 'fa-info-circle'} mr-2"></i>${message}`;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Load Profile
async function loadProfile() {
    try {
        const response = await fetch('/api/patient/profile');
        const data = await response.json();
        
        if (data.success) {
            // Profile data is returned directly in response, not under 'profile' key
            profileData = data;
            renderProfile();
        } else {
            showError('profileContent', data.message);
        }
    } catch (error) {
        console.error('Error loading profile:', error);
        showError('profileContent', 'Không thể tải thông tin cá nhân');
    }
}

// Render Profile
function renderProfile() {
    const container = document.getElementById('profileContent');
    
    container.innerHTML = `
        <div class="max-w-2xl mx-auto">
            <div class="bg-gradient-to-r from-blue-500 to-cyan-500 rounded-xl p-6 mb-6 text-white">
                <div class="flex items-center gap-4">
                    <div class="bg-white/20 p-4 rounded-full">
                        <i class="fas fa-user text-4xl"></i>
                    </div>
                    <div>
                        <h3 class="text-2xl font-bold">${profileData.fullName || 'Chưa cập nhật'}</h3>
                        <p class="text-white/80">${profileData.email || ''}</p>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="bg-gray-50 rounded-lg p-4">
                    <p class="text-sm text-gray-500 mb-1">Số điện thoại</p>
                    <p class="font-medium text-gray-800">${profileData.phoneNumber || 'Chưa cập nhật'}</p>
                </div>
                <div class="bg-gray-50 rounded-lg p-4">
                    <p class="text-sm text-gray-500 mb-1">Ngày sinh</p>
                    <p class="font-medium text-gray-800">${profileData.dateOfBirth ? formatDate(profileData.dateOfBirth) : 'Chưa cập nhật'}</p>
                </div>
                <div class="bg-gray-50 rounded-lg p-4">
                    <p class="text-sm text-gray-500 mb-1">Giới tính</p>
                    <p class="font-medium text-gray-800">${getGenderText(profileData.gender)}</p>
                </div>
                <div class="bg-gray-50 rounded-lg p-4">
                    <p class="text-sm text-gray-500 mb-1">Xác thực email</p>
                    <p class="font-medium text-gray-800">
                        ${profileData.emailVerified ? 
                            '<span class="text-green-600"><i class="fas fa-check-circle mr-1"></i>Đã xác thực</span>' : 
                            '<span class="text-red-600"><i class="fas fa-times-circle mr-1"></i>Chưa xác thực</span>'
                        }
                    </p>
                </div>
                <div class="bg-gray-50 rounded-lg p-4 md:col-span-2">
                    <p class="text-sm text-gray-500 mb-1">Địa chỉ</p>
                    <p class="font-medium text-gray-800">${profileData.address || 'Chưa cập nhật'}</p>
                </div>
                <div class="bg-gray-50 rounded-lg p-4 md:col-span-2">
                    <p class="text-sm text-gray-500 mb-1">Ngày tạo tài khoản</p>
                    <p class="font-medium text-gray-800">${formatDateTime(profileData.createdAt)}</p>
                </div>
            </div>

            <div class="mt-6 text-center">
                <a href="/profile" class="inline-block px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors">
                    <i class="fas fa-edit mr-2"></i>Chỉnh sửa thông tin
                </a>
            </div>
        </div>
    `;
}

// View Prescription Detail
async function viewPrescriptionDetail(id) {
    try {
        const response = await fetch(`/api/patient/prescriptions/${id}`);
        const data = await response.json();
        
        if (data.success) {
            const prescription = data.prescription;
            const modalContent = document.getElementById('prescriptionModalContent');
            
            modalContent.innerHTML = `
                <div class="space-y-4">
                    <div class="bg-blue-50 rounded-lg p-4">
                        <p class="text-sm text-gray-600 mb-1">Trạng thái</p>
                        <span class="px-3 py-1 rounded-full text-sm font-medium ${getStatusBadge(prescription.status)}">
                            ${getStatusText(prescription.status)}
                        </span>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div class="bg-gray-50 rounded-lg p-4">
                            <p class="text-sm text-gray-600 mb-1">Ngày kê đơn</p>
                            <p class="font-medium text-gray-800">${formatDate(prescription.prescriptionDate)}</p>
                        </div>
                        <div class="bg-gray-50 rounded-lg p-4">
                            <p class="text-sm text-gray-600 mb-1">Bác sĩ kê đơn</p>
                            <p class="font-medium text-gray-800">${prescription.doctor?.userInfo?.fullName || 'N/A'}</p>
                        </div>
                    </div>

                    <div class="bg-gray-50 rounded-lg p-4">
                        <p class="text-sm text-gray-600 mb-2">Chẩn đoán</p>
                        <p class="font-medium text-gray-800">${prescription.diagnosis || 'Không có'}</p>
                    </div>

                    ${prescription.notes ? `
                    <div class="bg-yellow-50 rounded-lg p-4">
                        <p class="text-sm text-gray-600 mb-2">Ghi chú</p>
                        <p class="text-gray-800">${prescription.notes}</p>
                    </div>
                    ` : ''}

                    ${prescription.details && prescription.details.length > 0 ? `
                    <div>
                        <h4 class="font-semibold text-gray-800 mb-3">Chi tiết thuốc</h4>
                        <div class="space-y-2">
                            ${prescription.details.map((detail, index) => `
                                <div class="border border-gray-200 rounded-lg p-4">
                                    <div class="flex items-start justify-between">
                                        <div class="flex-1">
                                            <p class="font-medium text-gray-800 mb-1">${index + 1}. ${detail.medicationName}</p>
                                            <p class="text-sm text-gray-600">Liều lượng: ${detail.dosage}</p>
                                            <p class="text-sm text-gray-600">Tần suất: ${detail.frequency}</p>
                                            <p class="text-sm text-gray-600">Thời gian: ${detail.duration}</p>
                                            ${detail.instructions ? `<p class="text-sm text-gray-600 mt-2"><i class="fas fa-info-circle mr-1"></i>${detail.instructions}</p>` : ''}
                                        </div>
                                    </div>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                    ` : '<p class="text-center text-gray-500 py-4">Chưa có chi tiết thuốc</p>'}
                </div>
            `;
            
            document.getElementById('prescriptionModal').classList.remove('hidden');
            document.getElementById('prescriptionModal').classList.add('flex');
        } else {
            alert(data.message || 'Không thể tải thông tin đơn thuốc');
        }
    } catch (error) {
        console.error('Error loading prescription detail:', error);
        alert('Không thể tải thông tin đơn thuốc');
    }
}

// Close Prescription Modal
function closePrescriptionModal() {
    document.getElementById('prescriptionModal').classList.add('hidden');
    document.getElementById('prescriptionModal').classList.remove('flex');
}

// Utility Functions
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
}

function getStatusBadge(status) {
    const badges = {
        'ACTIVE': 'bg-green-100 text-green-800',
        'COMPLETED': 'bg-blue-100 text-blue-800',
        'CANCELLED': 'bg-red-100 text-red-800'
    };
    return badges[status] || 'bg-gray-100 text-gray-800';
}

function getStatusText(status) {
    const texts = {
        'ACTIVE': 'Đang sử dụng',
        'COMPLETED': 'Hoàn thành',
        'CANCELLED': 'Đã hủy'
    };
    return texts[status] || status;
}

function getTreatmentStatusBadge(status) {
    const badges = {
        'ACTIVE': 'bg-green-100 text-green-800',
        'COMPLETED': 'bg-blue-100 text-blue-800',
        'PAUSED': 'bg-yellow-100 text-yellow-800',
        'CANCELLED': 'bg-red-100 text-red-800'
    };
    return badges[status] || 'bg-gray-100 text-gray-800';
}

function getTreatmentStatusText(status) {
    const texts = {
        'ACTIVE': 'Đang điều trị',
        'COMPLETED': 'Hoàn thành',
        'PAUSED': 'Tạm dừng',
        'CANCELLED': 'Đã hủy'
    };
    return texts[status] || status;
}

function getTicketStatusBadge(status) {
    const badges = {
        'OPEN': 'bg-yellow-100 text-yellow-800',
        'IN_PROGRESS': 'bg-blue-100 text-blue-800',
        'WAITING_DOCTOR': 'bg-purple-100 text-purple-800',
        'RESOLVED': 'bg-green-100 text-green-800',
        'CLOSED': 'bg-gray-100 text-gray-800'
    };
    return badges[status] || 'bg-gray-100 text-gray-800';
}

function getTicketStatusText(status) {
    const texts = {
        'OPEN': 'Mở',
        'IN_PROGRESS': 'Đang xử lý',
        'WAITING_DOCTOR': 'Chờ bác sĩ',
        'RESOLVED': 'Đã giải quyết',
        'CLOSED': 'Đã đóng'
    };
    return texts[status] || status;
}

function getPriorityBadge(priority) {
    const badges = {
        'URGENT': 'bg-red-100 text-red-800',
        'HIGH': 'bg-orange-100 text-orange-800',
        'MEDIUM': 'bg-yellow-100 text-yellow-800',
        'LOW': 'bg-green-100 text-green-800'
    };
    return badges[priority] || 'bg-gray-100 text-gray-800';
}

function getPriorityText(priority) {
    const texts = {
        'URGENT': 'Khẩn cấp',
        'HIGH': 'Cao',
        'MEDIUM': 'Trung bình',
        'LOW': 'Thấp'
    };
    return texts[priority] || priority;
}

function getGenderText(gender) {
    const texts = {
        'MALE': 'Nam',
        'FEMALE': 'Nữ',
        'OTHER': 'Khác'
    };
    return texts[gender] || 'Chưa cập nhật';
}

function showError(containerId, message) {
    document.getElementById(containerId).innerHTML = `
        <div class="text-center py-12">
            <i class="fas fa-exclamation-triangle text-6xl text-red-300 mb-4"></i>
            <p class="text-red-500 text-lg">${message}</p>
        </div>
    `;
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    loadStats();
    loadPrescriptions();
    loadProfile().then(() => {
        if (profileData && profileData.fullName) {
            document.getElementById('headerUserName').textContent = profileData.fullName;
        }
    });
});

// Close modal on outside click
document.getElementById('prescriptionModal')?.addEventListener('click', function(e) {
    if (e.target === this) {
        closePrescriptionModal();
    }
});

document.getElementById('uploadModal')?.addEventListener('click', function(e) {
    if (e.target === this) {
        closeUploadModal();
    }
});

document.getElementById('deleteModal')?.addEventListener('click', function(e) {
    if (e.target === this) {
        closeDeleteModal();
    }
});
