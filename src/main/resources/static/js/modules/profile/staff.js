/**
 * JavaScript cho trang Profile của Staff (Doctor, Receptionist, Admin)
 */

// API client
const apiClient = new APIClient();

// Lưu trữ thông tin profile ban đầu
let originalProfile = {};
let isEditing = false;

// Load profile khi trang vừa tải
document.addEventListener('DOMContentLoaded', () => {
    loadProfile();
    setupEventListeners();
});

/**
 * Load thông tin profile từ API
 */
async function loadProfile() {
    try {
        const response = await apiClient.get('/api/profile');
        
        if (!response.success) {
            throw new Error(response.message || 'Không thể tải thông tin profile');
        }

        const data = response.data;
        
        // Lưu dữ liệu gốc
        originalProfile = { ...data };
        
        // Hiển thị thông tin
        displayProfile(data);
        
    } catch (error) {
        console.error('Error loading profile:', error);
        showToast('Không thể tải thông tin profile', 'error');
    }
}

/**
 * Hiển thị thông tin profile lên form
 */
function displayProfile(data) {
    // Avatar
    const avatarImg = document.getElementById('avatarImg');
    const avatarPlaceholder = document.getElementById('avatarPlaceholder');
    if (data.avatar) {
        avatarImg.src = data.avatar;
        avatarImg.classList.remove('hidden');
        avatarPlaceholder.classList.add('hidden');
    } else {
        avatarImg.classList.add('hidden');
        avatarPlaceholder.classList.remove('hidden');
        // Lấy chữ cái đầu của tên
        const initials = data.fullName ? data.fullName.charAt(0).toUpperCase() : '?';
        avatarPlaceholder.textContent = initials;
    }
    
    // Header info
    document.getElementById('profileName').textContent = data.fullName || 'Chưa cập nhật';
    document.getElementById('profileRole').textContent = getRoleDisplayName(data.role);
    
    // Profile completion
    const completion = data.profileCompletion || 0;
    document.getElementById('completionPercent').textContent = completion + '%';
    document.getElementById('completionBar').style.width = completion + '%';
    
    // Update progress bar color
    const progressBar = document.getElementById('completionBar');
    if (completion >= 80) {
        progressBar.classList.remove('bg-warning-500', 'bg-error-500');
        progressBar.classList.add('bg-success-500');
    } else if (completion >= 50) {
        progressBar.classList.remove('bg-success-500', 'bg-error-500');
        progressBar.classList.add('bg-warning-500');
    } else {
        progressBar.classList.remove('bg-success-500', 'bg-warning-500');
        progressBar.classList.add('bg-error-500');
    }
    
    // Form fields
    document.getElementById('fullName').value = data.fullName || '';
    document.getElementById('phoneNumber').value = data.phone || '';
    document.getElementById('email').value = data.email || '';
    document.getElementById('dateOfBirth').value = data.dateOfBirth || '';
    document.getElementById('gender').value = data.gender || '';
    document.getElementById('address').value = data.address || '';
    
    // Email verification status
    const verificationBadge = document.getElementById('verificationBadge');
    if (data.isVerified) {
        verificationBadge.innerHTML = `
            <span class="inline-flex items-center gap-1.5 px-3 py-1 bg-success-50 text-success-700 text-sm font-medium rounded-lg">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
                Đã xác thực
            </span>
        `;
    } else {
        verificationBadge.innerHTML = `
            <span class="inline-flex items-center gap-1.5 px-3 py-1 bg-warning-50 text-warning-700 text-sm font-medium rounded-lg">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                </svg>
                Chưa xác thực
            </span>
        `;
    }
    
    // Password change availability
    const passwordSection = document.getElementById('passwordSection');
    if (data.canChangePassword) {
        passwordSection.classList.remove('hidden');
    } else {
        passwordSection.classList.add('hidden');
        // Show info message
        const infoMessage = document.createElement('div');
        infoMessage.className = 'mt-4 p-4 bg-info-50 text-info-700 rounded-xl';
        infoMessage.innerHTML = `
            <p class="text-sm">
                <strong>Lưu ý:</strong> Tài khoản đăng nhập bằng Google không thể đổi mật khẩu tại đây.
            </p>
        `;
        document.getElementById('securityCard').appendChild(infoMessage);
    }
}

/**
 * Setup các event listeners
 */
function setupEventListeners() {
    // Avatar upload
    const avatarInput = document.getElementById('avatarInput');
    const avatarUploadBtn = document.getElementById('avatarUploadBtn');
    
    if (avatarUploadBtn && avatarInput) {
        avatarUploadBtn.addEventListener('click', () => avatarInput.click());
        avatarInput.addEventListener('change', handleAvatarUpload);
    }
    
    // Edit button
    const editBtn = document.getElementById('editBtn');
    if (editBtn) {
        editBtn.addEventListener('click', enableEditing);
    }
    
    // Cancel button
    const cancelBtn = document.getElementById('cancelBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelEditing);
    }
    
    // Profile form submit
    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', handleProfileSubmit);
    }
    
    // Password form submit
    const passwordForm = document.getElementById('passwordForm');
    if (passwordForm) {
        passwordForm.addEventListener('submit', handlePasswordSubmit);
    }
}

/**
 * Bật chế độ chỉnh sửa
 */
function enableEditing() {
    isEditing = true;
    
    // Enable form fields (trừ email)
    document.getElementById('fullName').disabled = false;
    document.getElementById('phoneNumber').disabled = false;
    document.getElementById('dateOfBirth').disabled = false;
    document.getElementById('gender').disabled = false;
    document.getElementById('address').disabled = false;
    
    // Hiện action buttons
    document.getElementById('formActions').classList.remove('hidden');
    
    // Ẩn edit button
    document.getElementById('editBtn').classList.add('hidden');
}

/**
 * Hủy chỉnh sửa và khôi phục dữ liệu gốc
 */
function cancelEditing() {
    isEditing = false;
    
    // Disable form fields
    document.getElementById('fullName').disabled = true;
    document.getElementById('phoneNumber').disabled = true;
    document.getElementById('dateOfBirth').disabled = true;
    document.getElementById('gender').disabled = true;
    document.getElementById('address').disabled = true;
    
    // Khôi phục dữ liệu gốc
    displayProfile(originalProfile);
    
    // Ẩn action buttons
    document.getElementById('formActions').classList.add('hidden');
    
    // Hiện edit button
    document.getElementById('editBtn').classList.remove('hidden');
}

/**
 * Xử lý submit form cập nhật profile
 */
async function handleProfileSubmit(e) {
    e.preventDefault();
    
    if (!isEditing) return;
    
    const submitBtn = document.getElementById('saveBtn');
    const originalText = submitBtn.innerHTML;
    
    try {
        // Show loading
        submitBtn.disabled = true;
        submitBtn.innerHTML = `
            <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
        `;
        
        // Lấy dữ liệu từ form
        const formData = {
            fullName: document.getElementById('fullName').value.trim(),
            phoneNumber: document.getElementById('phoneNumber').value.trim(),
            dateOfBirth: document.getElementById('dateOfBirth').value || null,
            gender: document.getElementById('gender').value || null,
            address: document.getElementById('address').value.trim() || null
        };
        
        // Validate
        if (!formData.fullName) {
            showToast('Vui lòng nhập họ tên', 'error');
            return;
        }
        
        if (formData.phoneNumber && !formData.phoneNumber.match(/^0\d{9}$/)) {
            showToast('Số điện thoại không hợp lệ. Vui lòng nhập 10 chữ số bắt đầu bằng 0', 'error');
            return;
        }
        
        // Call API
        const response = await apiClient.post('/api/profile/update', formData);
        
        if (response.success) {
            showToast('Cập nhật thông tin thành công', 'success');
            
            // Update original profile
            originalProfile = { ...response.data };
            
            // Disable editing mode
            cancelEditing();
            
            // Refresh display
            displayProfile(response.data);
        } else {
            throw new Error(response.message || 'Có lỗi xảy ra');
        }
        
    } catch (error) {
        console.error('Error updating profile:', error);
        showToast(error.message || 'Không thể cập nhật thông tin', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

/**
 * Xử lý submit form đổi mật khẩu
 */
async function handlePasswordSubmit(e) {
    e.preventDefault();
    
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    
    try {
        // Show loading
        submitBtn.disabled = true;
        submitBtn.innerHTML = `
            <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
        `;
        
        // Lấy dữ liệu từ form
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        
        // Validate
        if (!currentPassword || !newPassword || !confirmPassword) {
            showToast('Vui lòng điền đầy đủ thông tin', 'error');
            return;
        }
        
        if (newPassword.length < 6) {
            showToast('Mật khẩu mới phải có ít nhất 6 ký tự', 'error');
            return;
        }
        
        if (newPassword !== confirmPassword) {
            showToast('Mật khẩu xác nhận không khớp', 'error');
            return;
        }
        
        // Call API
        const response = await apiClient.post('/api/profile/change-password', {
            currentPassword,
            newPassword
        });
        
        if (response.success) {
            showToast('Đổi mật khẩu thành công', 'success');
            
            // Reset form
            e.target.reset();
        } else {
            throw new Error(response.message || 'Có lỗi xảy ra');
        }
        
    } catch (error) {
        console.error('Error changing password:', error);
        showToast(error.message || 'Không thể đổi mật khẩu', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

/**
 * Xử lý upload avatar
 */
async function handleAvatarUpload(e) {
    const file = e.target.files[0];
    if (!file) return;
    
    // Validate file type
    if (!file.type.match(/^image\/(jpeg|jpg|png)$/)) {
        showToast('Chỉ chấp nhận file ảnh định dạng JPG, JPEG, PNG', 'error');
        e.target.value = '';
        return;
    }
    
    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
        showToast('Kích thước file không được vượt quá 5MB', 'error');
        e.target.value = '';
        return;
    }
    
    try {
        // Show loading overlay on avatar
        const avatarContainer = document.querySelector('.relative.group');
        const loadingOverlay = document.createElement('div');
        loadingOverlay.className = 'absolute inset-0 bg-black bg-opacity-50 rounded-full flex items-center justify-center';
        loadingOverlay.innerHTML = `
            <svg class="animate-spin h-8 w-8 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
        `;
        avatarContainer.appendChild(loadingOverlay);
        
        // Create FormData
        const formData = new FormData();
        formData.append('file', file);
        
        // Call API
        const response = await fetch('/api/profile/avatar', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            showToast('Upload avatar thành công', 'success');
            
            // Update avatar display
            const avatarImg = document.getElementById('avatarImg');
            const avatarPlaceholder = document.getElementById('avatarPlaceholder');
            avatarImg.src = result.avatarUrl;
            avatarImg.classList.remove('hidden');
            avatarPlaceholder.classList.add('hidden');
            
            // Reload profile to update completion
            loadProfile();
        } else {
            throw new Error(result.message || 'Có lỗi xảy ra');
        }
        
        // Remove loading overlay
        loadingOverlay.remove();
        
    } catch (error) {
        console.error('Error uploading avatar:', error);
        showToast(error.message || 'Không thể upload avatar', 'error');
    } finally {
        e.target.value = '';
    }
}

/**
 * Helper: Lấy tên hiển thị của role
 */
function getRoleDisplayName(role) {
    const roleNames = {
        'PATIENT': 'Bệnh nhân',
        'DOCTOR': 'Bác sĩ',
        'RECEPTIONIST': 'Lễ tân',
        'ADMIN': 'Quản trị viên'
    };
    return roleNames[role] || role;
}

/**
 * Hiển thị toast notification
 */
function showToast(message, type = 'info') {
    // Tạo toast container nếu chưa có
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'fixed top-4 right-4 z-50 space-y-2';
        document.body.appendChild(container);
    }
    
    // Màu sắc theo type
    const colors = {
        success: 'bg-success-500',
        error: 'bg-error-500',
        warning: 'bg-warning-500',
        info: 'bg-info-500'
    };
    
    // Icons theo type
    const icons = {
        success: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>',
        error: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>',
        warning: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>',
        info: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>'
    };
    
    // Tạo toast element
    const toast = document.createElement('div');
    toast.className = `${colors[type]} text-white px-6 py-4 rounded-xl shadow-lg flex items-center gap-3 animate-slideDown`;
    toast.innerHTML = `
        <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            ${icons[type]}
        </svg>
        <span class="font-medium">${message}</span>
    `;
    
    container.appendChild(toast);
    
    // Auto remove sau 3s
    setTimeout(() => {
        toast.classList.add('animate-fadeOut');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}
