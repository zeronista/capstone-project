/**
 * Patient Management Module for Receptionist Dashboard
 * Handles CRUD operations and Excel import for PATIENTS only
 */

// ==================== Global State ====================
let allUsers = [];
let filteredUsers = [];
let currentPage = 1;
const pageSize = 10;
let selectedFile = null;
let deleteUserId = null;
let isEditMode = false;

// ==================== Initialize ====================
document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
    initEventListeners();
});

function initEventListeners() {
    // Search with debounce
    const searchInput = document.getElementById('searchInput');
    let searchTimeout;
    searchInput.addEventListener('input', () => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            filterUsers();
        }, 300);
    });

    // Filter change handlers
    document.getElementById('statusFilter').addEventListener('change', filterUsers);

    // Keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeModal();
            closeImportModal();
            closeDeleteModal();
        }
    });
}

// ==================== Load Users ====================
async function loadUsers() {
    showLoading(true);
    try {
        // Only load PATIENT role for receptionist
        const response = await fetch('/api/users?role=PATIENT', {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Không thể tải danh sách bệnh nhân');
        }

        allUsers = await response.json();
        filterUsers();
    } catch (error) {
        console.error('Error loading users:', error);
        showToast('Lỗi tải dữ liệu: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// ==================== Filter & Search ====================
function filterUsers() {
    const keyword = document.getElementById('searchInput').value.toLowerCase().trim();
    const statusFilter = document.getElementById('statusFilter').value;

    filteredUsers = allUsers.filter(user => {
        // Text search
        const matchesKeyword = !keyword || 
            (user.fullName && user.fullName.toLowerCase().includes(keyword)) ||
            (user.email && user.email.toLowerCase().includes(keyword)) ||
            (user.phoneNumber && user.phoneNumber.includes(keyword));

        // Status filter
        const matchesStatus = !statusFilter || 
            (statusFilter === 'active' && user.isActive) ||
            (statusFilter === 'inactive' && !user.isActive);

        return matchesKeyword && matchesStatus;
    });

    currentPage = 1;
    renderUsers();
}

// ==================== Render Table ====================
function renderUsers() {
    const tbody = document.getElementById('usersTableBody');
    const emptyState = document.getElementById('emptyState');

    if (filteredUsers.length === 0) {
        tbody.innerHTML = '';
        emptyState.classList.remove('hidden');
        document.getElementById('paginationContainer').classList.add('hidden');
        return;
    }

    emptyState.classList.add('hidden');
    document.getElementById('paginationContainer').classList.remove('hidden');

    // Calculate pagination
    const start = (currentPage - 1) * pageSize;
    const end = Math.min(start + pageSize, filteredUsers.length);
    const pageUsers = filteredUsers.slice(start, end);

    tbody.innerHTML = pageUsers.map(user => `
        <tr class="hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors border-b border-slate-100 dark:border-slate-700/50">
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10">
                        ${user.avatarUrl 
                            ? `<img class="h-10 w-10 rounded-full object-cover" src="${user.avatarUrl}" alt="${user.fullName || 'Avatar'}">`
                            : `<div class="h-10 w-10 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center">
                                <span class="text-white font-medium text-sm">${getInitials(user.fullName || user.email)}</span>
                               </div>`
                        }
                    </div>
                    <div class="ml-4">
                        <div class="text-sm font-medium text-slate-900 dark:text-white">${user.fullName || 'Chưa cập nhật'}</div>
                    </div>
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-slate-900 dark:text-white">${user.email || '-'}</div>
                <div class="text-sm text-slate-500 dark:text-slate-400">${user.phoneNumber || '-'}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                ${getStatusBadge(user.isActive)}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex items-center justify-end gap-2">
                    <button onclick="viewPatientDetail(${user.id})"
                        class="text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white p-1.5 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-700/60 transition-colors"
                        title="Xem hồ sơ">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                        </svg>
                    </button>
                    <button onclick="showEditModal(${user.id})" 
                        class="text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 p-1.5 rounded-lg hover:bg-blue-50 dark:hover:bg-blue-900/30 transition-colors"
                        title="Chỉnh sửa">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                    </button>
                    ${user.isActive 
                        ? `<button onclick="showDeleteModal(${user.id}, '${escapeHtml(user.fullName || user.email)}')" 
                            class="text-red-600 dark:text-red-400 hover:text-red-900 dark:hover:text-red-300 p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/30 transition-colors"
                            title="Vô hiệu hóa">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path>
                            </svg>
                        </button>`
                        : `<button onclick="restoreUser(${user.id})" 
                            class="text-green-600 dark:text-green-400 hover:text-green-900 dark:hover:text-green-300 p-1.5 rounded-lg hover:bg-green-50 dark:hover:bg-green-900/30 transition-colors"
                            title="Khôi phục">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                            </svg>
                        </button>`
                    }
                </div>
            </td>
        </tr>
    `).join('');

    renderPagination();
}

function renderPagination() {
    const totalPages = Math.ceil(filteredUsers.length / pageSize);
    const paginationNav = document.getElementById('paginationNav');

    // Update info text
    const start = (currentPage - 1) * pageSize + 1;
    const end = Math.min(currentPage * pageSize, filteredUsers.length);
    document.getElementById('showingFrom').textContent = start;
    document.getElementById('showingTo').textContent = end;
    document.getElementById('totalCount').textContent = filteredUsers.length;

    // Generate pagination buttons
    let buttons = '';

    // Previous button
    buttons += `
        <button onclick="goToPage(${currentPage - 1})" 
            class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-sm font-medium ${currentPage === 1 ? 'text-slate-300 dark:text-slate-600 cursor-not-allowed' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700'}"
            ${currentPage === 1 ? 'disabled' : ''}>
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
        </button>
    `;

    // Page numbers
    const maxVisiblePages = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
        const isActive = i === currentPage;
        buttons += `
            <button onclick="goToPage(${i})" 
                class="relative inline-flex items-center px-4 py-2 border text-sm font-medium ${isActive 
                    ? 'z-10 bg-blue-50 dark:bg-blue-900/30 border-blue-500 text-blue-600 dark:text-blue-400' 
                    : 'bg-white dark:bg-slate-800 border-slate-300 dark:border-slate-600 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700'}">
                ${i}
            </button>
        `;
    }

    // Next button
    buttons += `
        <button onclick="goToPage(${currentPage + 1})" 
            class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-sm font-medium ${currentPage === totalPages ? 'text-slate-300 dark:text-slate-600 cursor-not-allowed' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700'}"
            ${currentPage === totalPages ? 'disabled' : ''}>
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
        </button>
    `;

    paginationNav.innerHTML = buttons;
}

function goToPage(page) {
    const totalPages = Math.ceil(filteredUsers.length / pageSize);
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    renderUsers();
}

// ==================== Modal Functions ====================
function showCreateModal() {
    isEditMode = false;
    document.getElementById('modalTitle').textContent = 'Thêm bệnh nhân mới';
    document.getElementById('submitBtnText').textContent = 'Tạo bệnh nhân';
    document.getElementById('passwordField').classList.remove('hidden');
    document.getElementById('statusField').classList.add('hidden');
    document.getElementById('userForm').reset();
    document.getElementById('userId').value = '';
    document.getElementById('userModal').classList.remove('hidden');
}

async function showEditModal(userId) {
    isEditMode = true;
    const user = allUsers.find(u => u.id === userId);
    if (!user) {
        showToast('Không tìm thấy người dùng', 'error');
        return;
    }

    document.getElementById('modalTitle').textContent = 'Chỉnh sửa bệnh nhân';
    document.getElementById('submitBtnText').textContent = 'Cập nhật';
    document.getElementById('passwordField').querySelector('label').innerHTML = 
        'Mật khẩu mới <span class="text-slate-400 dark:text-slate-500 text-xs">(để trống nếu không đổi)</span>';
    document.getElementById('statusField').classList.remove('hidden');

    // Fill form
    document.getElementById('userId').value = user.id;
    document.getElementById('fullName').value = user.fullName || '';
    document.getElementById('email').value = user.email || '';
    document.getElementById('phone').value = user.phoneNumber || '';
    document.getElementById('role').value = user.role || '';
    document.getElementById('dateOfBirth').value = user.dateOfBirth || '';
    document.getElementById('gender').value = user.gender || '';
    document.getElementById('address').value = user.address || '';
    document.getElementById('password').value = '';
    document.getElementById('isActive').checked = user.isActive;

    document.getElementById('userModal').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('userModal').classList.add('hidden');
}

// ==================== CRUD Operations ====================
async function handleSubmitUser(event) {
    event.preventDefault();

    const submitBtn = document.getElementById('submitBtn');
    const submitBtnText = document.getElementById('submitBtnText');
    const submitBtnLoading = document.getElementById('submitBtnLoading');

    // Validate email or phone required
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    if (!email && !phone) {
        showToast('Vui lòng nhập email hoặc số điện thoại', 'error');
        return;
    }

    // Show loading
    submitBtn.disabled = true;
    submitBtnText.classList.add('opacity-50');
    submitBtnLoading.classList.remove('hidden');

    const formData = {
        fullName: document.getElementById('fullName').value.trim(),
        email: email || null,
        phone: phone || null,
        role: document.getElementById('role').value,
        dateOfBirth: document.getElementById('dateOfBirth').value || null,
        gender: document.getElementById('gender').value || null,
        address: document.getElementById('address').value.trim() || null,
        password: document.getElementById('password').value || null
    };

    try {
        const userId = document.getElementById('userId').value;
        let response;

        if (isEditMode && userId) {
            // Update
            formData.isActive = document.getElementById('isActive').checked;
            formData.newPassword = formData.password;
            delete formData.password;

            response = await fetch(`/api/users/${userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        } else {
            // Create
            response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        }

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || 'Có lỗi xảy ra');
        }

        closeModal();
        showToast(isEditMode ? 'Cập nhật người dùng thành công!' : 'Tạo người dùng thành công!', 'success');
        loadUsers();

    } catch (error) {
        console.error('Error saving user:', error);
        showToast(error.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtnText.classList.remove('opacity-50');
        submitBtnLoading.classList.add('hidden');
    }
}

function showDeleteModal(userId, userName) {
    deleteUserId = userId;
    document.getElementById('deleteUserName').textContent = userName;
    document.getElementById('deleteModal').classList.remove('hidden');
}

function closeDeleteModal() {
    deleteUserId = null;
    document.getElementById('deleteModal').classList.add('hidden');
}

async function confirmDelete() {
    if (!deleteUserId) return;

    try {
        const response = await fetch(`/api/users/${deleteUserId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const result = await response.json();
            throw new Error(result.error || 'Có lỗi xảy ra');
        }

        closeDeleteModal();
        showToast('Đã vô hiệu hóa người dùng!', 'success');
        loadUsers();

    } catch (error) {
        console.error('Error deleting user:', error);
        showToast(error.message, 'error');
    }
}

async function restoreUser(userId) {
    try {
        const response = await fetch(`/api/users/${userId}/restore`, {
            method: 'POST'
        });

        if (!response.ok) {
            const result = await response.json();
            throw new Error(result.error || 'Có lỗi xảy ra');
        }

        showToast('Đã khôi phục người dùng!', 'success');
        loadUsers();

    } catch (error) {
        console.error('Error restoring user:', error);
        showToast(error.message, 'error');
    }
}

// ==================== Patient Detail Modal ====================
async function viewPatientDetail(userId) {
    try {
        const response = await fetch(`/api/receptionist/patients/${userId}/detail`, {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Không thể tải hồ sơ bệnh nhân');
        }

        const result = await response.json();
        if (!result.success || !result.data) {
            throw new Error(result.message || 'Không thể tải hồ sơ bệnh nhân');
        }

        const { patient, healthProfile, stats, labReports } = result.data;

        // Basic info
        document.getElementById('patientDetailName').textContent = patient.fullName || 'Chưa cập nhật';
        document.getElementById('patientDetailEmail').textContent = patient.email || '-';
        document.getElementById('patientDetailPhone').textContent = patient.phoneNumber || '-';
        document.getElementById('patientDetailVisitCount').textContent =
            (stats && typeof stats.visitCount === 'number') ? stats.visitCount : 0;

        // Health profile
        const bloodType = healthProfile && healthProfile.bloodType ? healthProfile.bloodType : 'Chưa cập nhật';
        const height = healthProfile && healthProfile.heightCm != null ? `${healthProfile.heightCm} cm` : '-';
        const weight = healthProfile && healthProfile.weightKg != null ? `${healthProfile.weightKg} kg` : '-';

        document.getElementById('patientDetailBloodType').textContent = bloodType;
        document.getElementById('patientDetailHeight').textContent = height;
        document.getElementById('patientDetailWeight').textContent = weight;

        // History (allergies + chronic diseases)
        let historyText = '';
        if (healthProfile) {
            if (healthProfile.chronicDiseases) {
                historyText += `Tiền sử bệnh:\n${healthProfile.chronicDiseases}\n\n`;
            }
            if (healthProfile.allergies) {
                historyText += `Dị ứng:\n${healthProfile.allergies}\n`;
            }
        }
        document.getElementById('patientDetailHistory').textContent =
            historyText.trim() || 'Chưa có thông tin.';

        // Avatar
        const avatarImg = document.getElementById('patientDetailAvatarImg');
        const avatarFallback = document.getElementById('patientDetailAvatarFallback');
        const avatarInitials = document.getElementById('patientDetailAvatarInitials');
        if (patient.avatarUrl) {
            avatarImg.src = patient.avatarUrl;
            avatarImg.classList.remove('hidden');
            avatarFallback.classList.add('hidden');
        } else {
            avatarImg.classList.add('hidden');
            avatarFallback.classList.remove('hidden');
            avatarInitials.textContent = getInitials(patient.fullName || patient.email);
        }

        // Lab results
        const labList = document.getElementById('patientDetailLabList');
        const labEmpty = document.getElementById('patientDetailLabEmpty');
        const labCount = document.getElementById('patientDetailLabCount');
        labList.innerHTML = '';

        const items = Array.isArray(labReports) ? labReports : [];
        labCount.textContent = `${items.length} kết quả`;

        if (items.length === 0) {
            labEmpty.classList.remove('hidden');
        } else {
            labEmpty.classList.add('hidden');
            labList.innerHTML = items
                .slice(0, 20)
                .map(report => {
                    const date = report.reportDate ? new Date(report.reportDate).toLocaleDateString('vi-VN') : '-';
                    const title = report.title || 'Kết quả xét nghiệm';
                    const notes = report.notes || '';
                    return `
                        <li class="py-2.5 flex items-start justify-between gap-3">
                            <div>
                                <p class="font-medium text-slate-900 dark:text-white">${title}</p>
                                <p class="text-xs text-slate-500 dark:text-slate-400">Ngày: ${date}</p>
                                ${notes ? `<p class="mt-1 text-xs text-slate-500 dark:text-slate-400">${notes}</p>` : ''}
                            </div>
                            ${report.fileUrl ? `
                                <a href="${report.fileUrl}" target="_blank"
                                   class="inline-flex items-center text-xs text-primary hover:text-primary-dark font-medium">
                                    Xem file
                                </a>
                            ` : ''}
                        </li>
                    `;
                })
                .join('');
        }

        // Show modal
        document.getElementById('patientDetailModal').classList.remove('hidden');
    } catch (error) {
        console.error('Error loading patient detail:', error);
        showToast(error.message || 'Không thể tải hồ sơ bệnh nhân', 'error');
    }
}

function closePatientDetailModal() {
    const modal = document.getElementById('patientDetailModal');
    if (modal) {
        modal.classList.add('hidden');
    }
}

// ==================== Import Modal Functions ====================
function showImportModal() {
    resetImportModal();
    document.getElementById('importModal').classList.remove('hidden');
}

function closeImportModal() {
    document.getElementById('importModal').classList.add('hidden');
    resetImportModal();
}

function resetImportModal() {
    selectedFile = null;
    document.getElementById('fileInput').value = '';
    document.getElementById('uploadArea').classList.remove('hidden');
    document.getElementById('selectedFileInfo').classList.add('hidden');
    document.getElementById('importProgress').classList.add('hidden');
    document.getElementById('importResults').classList.add('hidden');
    document.getElementById('importBtn').disabled = true;
}

// ==================== File Handling ====================
function handleDragOver(event) {
    event.preventDefault();
    event.currentTarget.classList.add('border-blue-500', 'bg-blue-50');
}

function handleDragLeave(event) {
    event.preventDefault();
    event.currentTarget.classList.remove('border-blue-500', 'bg-blue-50');
}

function handleDrop(event) {
    event.preventDefault();
    event.currentTarget.classList.remove('border-blue-500', 'bg-blue-50');
    
    const files = event.dataTransfer.files;
    if (files.length > 0) {
        handleFile(files[0]);
    }
}

function handleFileSelect(event) {
    const file = event.target.files[0];
    if (file) {
        handleFile(file);
    }
}

function handleFile(file) {
    const validTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel'];
    
    if (!validTypes.includes(file.type) && !file.name.match(/\.(xlsx|xls)$/i)) {
        showToast('Chỉ chấp nhận file Excel (.xlsx, .xls)', 'error');
        return;
    }

    selectedFile = file;
    document.getElementById('uploadArea').classList.add('hidden');
    document.getElementById('selectedFileInfo').classList.remove('hidden');
    document.getElementById('fileName').textContent = file.name;
    document.getElementById('fileSize').textContent = formatFileSize(file.size);
    document.getElementById('importBtn').disabled = false;
}

function clearSelectedFile() {
    selectedFile = null;
    document.getElementById('fileInput').value = '';
    document.getElementById('uploadArea').classList.remove('hidden');
    document.getElementById('selectedFileInfo').classList.add('hidden');
    document.getElementById('importBtn').disabled = true;
}

// ==================== Import Process ====================
async function handleImport() {
    if (!selectedFile) {
        showToast('Vui lòng chọn file', 'error');
        return;
    }

    const importBtn = document.getElementById('importBtn');
    const importBtnText = document.getElementById('importBtnText');
    const importBtnLoading = document.getElementById('importBtnLoading');

    importBtn.disabled = true;
    importBtnText.textContent = 'Đang import...';
    importBtnLoading.classList.remove('hidden');

    document.getElementById('importProgress').classList.remove('hidden');

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
        // Simulate progress
        simulateProgress();

        const response = await fetch('/api/users/import', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || 'Có lỗi xảy ra khi import');
        }

        // Show results
        showImportResults(result);
        
        // Reload users if any were created
        if (result.successCount > 0) {
            loadUsers();
        }

    } catch (error) {
        console.error('Error importing file:', error);
        showToast(error.message, 'error');
    } finally {
        importBtn.disabled = false;
        importBtnText.textContent = 'Import';
        importBtnLoading.classList.add('hidden');
        document.getElementById('importProgress').classList.add('hidden');
    }
}

function simulateProgress() {
    const progressBar = document.getElementById('progressBar');
    const progressPercent = document.getElementById('progressPercent');
    let progress = 0;

    const interval = setInterval(() => {
        progress += Math.random() * 20;
        if (progress > 90) {
            clearInterval(interval);
            progress = 90;
        }
        progressBar.style.width = `${progress}%`;
        progressPercent.textContent = `${Math.round(progress)}%`;
    }, 200);

    // Complete progress when done
    setTimeout(() => {
        clearInterval(interval);
        progressBar.style.width = '100%';
        progressPercent.textContent = '100%';
    }, 2000);
}

function showImportResults(result) {
    const resultsDiv = document.getElementById('importResults');
    const summaryDiv = document.getElementById('resultSummary');
    const errorList = document.getElementById('errorList');
    const errorListItems = document.getElementById('errorListItems');

    const hasErrors = result.errorCount > 0;
    
    resultsDiv.classList.remove('hidden');
    resultsDiv.className = `mt-4 p-4 rounded-lg ${hasErrors ? 'bg-yellow-50 border border-yellow-200' : 'bg-green-50 border border-green-200'}`;

    summaryDiv.innerHTML = `
        <div class="flex items-center">
            ${hasErrors 
                ? `<svg class="w-5 h-5 text-yellow-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                   </svg>`
                : `<svg class="w-5 h-5 text-green-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                   </svg>`
            }
            <span class="${hasErrors ? 'text-yellow-700' : 'text-green-700'} font-medium">
                Import hoàn tất: ${result.successCount}/${result.totalRows} thành công
            </span>
        </div>
    `;

    if (hasErrors && result.errors && result.errors.length > 0) {
        errorList.classList.remove('hidden');
        errorListItems.innerHTML = result.errors.slice(0, 10).map(error => 
            `<li>Dòng ${error.rowNumber}: ${error.message}</li>`
        ).join('');

        if (result.errors.length > 10) {
            errorListItems.innerHTML += `<li class="text-slate-500 dark:text-slate-400">... và ${result.errors.length - 10} lỗi khác</li>`;
        }
    } else {
        errorList.classList.add('hidden');
    }
}

// ==================== Helper Functions ====================
function showLoading(show) {
    document.getElementById('loadingState').classList.toggle('hidden', !show);
}

function getRoleBadge(role) {
    const badges = {
        ADMIN: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-300">Admin</span>',
        DOCTOR: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300">Bác sĩ</span>',
        RECEPTIONIST: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300">Lễ tân</span>',
        PATIENT: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 dark:bg-slate-700 text-slate-800 dark:text-slate-300">Bệnh nhân</span>'
    };
    return badges[role] || `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 dark:bg-slate-700 text-slate-800 dark:text-slate-300">${role}</span>`;
}

function getStatusBadge(isActive) {
    if (isActive) {
        return '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"><span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-1.5"></span>Hoạt động</span>';
    }
    return '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300"><span class="w-1.5 h-1.5 bg-red-500 rounded-full mr-1.5"></span>Vô hiệu</span>';
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function getInitials(name) {
    if (!name) return '?';
    const parts = name.split(' ').filter(p => p.length > 0);
    if (parts.length >= 2) {
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ==================== Toast Notification ====================
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    const toastIcon = document.getElementById('toastIcon');

    toastMessage.textContent = message;

    const icons = {
        success: '<svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>',
        error: '<svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>',
        info: '<svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>'
    };

    toastIcon.innerHTML = icons[type] || icons.info;

    toast.classList.remove('hidden', 'translate-y-full', 'opacity-0');
    toast.classList.add('translate-y-0', 'opacity-100');

    setTimeout(hideToast, 4000);
}

function hideToast() {
    const toast = document.getElementById('toast');
    toast.classList.remove('translate-y-0', 'opacity-100');
    toast.classList.add('translate-y-full', 'opacity-0');
    setTimeout(() => toast.classList.add('hidden'), 300);
}
