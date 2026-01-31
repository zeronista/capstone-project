/**
 * JavaScript cho trang Profile của bệnh nhân
 */

// Lưu trữ thông tin profile ban đầu
let originalProfile = {};
let isEditing = false;

// Load profile khi trang vừa tải
document.addEventListener('DOMContentLoaded', () => {
    loadProfile();
    checkEmailVerificationStatus();
    setupEventListeners();
    setupAvatarUpload();
});

/**
 * Load thông tin profile từ API
 */
async function loadProfile() {
    try {
        const response = await fetch('/api/profile');
        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = '/auth/login';
                return;
            }
            throw new Error('Không thể tải thông tin profile');
        }

        const result = await response.json();
        
        if (!result.success) {
            throw new Error(result.message || 'Không thể tải thông tin profile');
        }
        
        const data = result.data;
        
        // Lưu dữ liệu gốc
        originalProfile = { ...data };
        
        // Hiển thị thông tin
        displayProfile(data);
        
    } catch (error) {
        console.error('Error loading profile:', error);
        showMessage('Không thể tải thông tin profile', 'error');
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
    }
    
    // Profile completion
    const completion = data.profileCompletion || 0;
    document.getElementById('completionPercent').textContent = completion + '%';
    document.getElementById('completionBar').style.width = completion + '%';
    
    // Update progress bar color
    const progressBar = document.getElementById('completionBar');
    if (completion >= 80) {
        progressBar.classList.remove('bg-yellow-500', 'bg-red-500');
        progressBar.classList.add('bg-blue-600');
    } else if (completion >= 50) {
        progressBar.classList.remove('bg-blue-600', 'bg-red-500');
        progressBar.classList.add('bg-yellow-500');
    } else {
        progressBar.classList.remove('bg-blue-600', 'bg-yellow-500');
        progressBar.classList.add('bg-red-500');
    }
    
    // Header
    document.getElementById('profileName').textContent = data.fullName || 'Chưa cập nhật';
    document.getElementById('userName').textContent = data.fullName || 'Bệnh nhân';
    
    if (data.createdAt) {
        const createdDate = new Date(data.createdAt);
        document.getElementById('createdDate').textContent = formatDate(createdDate);
    }
    
    // Form fields
    document.getElementById('fullName').value = data.fullName || '';
    document.getElementById('phoneNumber').value = data.phoneNumber || '';
    document.getElementById('email').value = data.email || '';
    document.getElementById('dateOfBirth').value = data.dateOfBirth || '';
    document.getElementById('gender').value = data.gender || '';
    document.getElementById('address').value = data.address || '';
}

/**
 * Setup các event listeners
 */
function setupEventListeners() {
    // Edit button
    document.getElementById('editBtn').addEventListener('click', enableEditing);
    
    // Cancel button
    document.getElementById('cancelBtn').addEventListener('click', cancelEditing);
    
    // Profile form submit
    document.getElementById('profileForm').addEventListener('submit', handleProfileSubmit);
    
    // Password form submit
    document.getElementById('passwordForm').addEventListener('submit', handlePasswordSubmit);
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
 * Hủy chỉnh sửa
 */
function cancelEditing() {
    isEditing = false;
    
    // Disable form fields
    document.getElementById('fullName').disabled = true;
    document.getElementById('phoneNumber').disabled = true;
    document.getElementById('dateOfBirth').disabled = true;
    document.getElementById('gender').disabled = true;
    document.getElementById('address').disabled = true;
    
    // Ẩn action buttons
    document.getElementById('formActions').classList.add('hidden');
    
    // Hiện edit button
    document.getElementById('editBtn').classList.remove('hidden');
    
    // Khôi phục dữ liệu gốc
    displayProfile(originalProfile);
}

/**
 * Xử lý submit form cập nhật profile
 */
async function handleProfileSubmit(e) {
    e.preventDefault();
    
    if (!isEditing) return;
    
    const formData = {
        fullName: document.getElementById('fullName').value.trim(),
        phoneNumber: document.getElementById('phoneNumber').value.trim(),
        dateOfBirth: document.getElementById('dateOfBirth').value || null,
        gender: document.getElementById('gender').value || null,
        address: document.getElementById('address').value.trim() || null
    };
    
    // Validation
    // Validate full name
    if (!formData.fullName || formData.fullName.trim() === '') {
        showMessage('Vui lòng nhập họ tên', 'error');
        return;
    }
    
    // Validate phone number
    if (!formData.phoneNumber) {
        showMessage('Vui lòng nhập số điện thoại', 'error');
        return;
    }
    
    // Validate phone format (10 digits starting with 0)
    if (!/^0\d{9}$/.test(formData.phoneNumber)) {
        showMessage('Số điện thoại phải có 10 chữ số và bắt đầu bằng 0', 'error');
        return;
    }
    
    // Show loading state
    const submitBtn = document.querySelector('#profileForm button[type="submit"]');
    const originalBtnText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Đang lưu...';
    
    try {
        const response = await fetch('/api/profile/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            showMessage('✅ Cập nhật thông tin thành công', 'success');
            
            // Cập nhật dữ liệu gốc
            originalProfile = { ...result.data };
            
            // Thoát chế độ chỉnh sửa
            cancelEditing();
            
            // Cập nhật hiển thị
            displayProfile(result.data);
            
            // Reload profile to ensure fresh data
            setTimeout(() => loadProfile(), 500);
        } else {
            showMessage('❌ ' + (result.message || 'Cập nhật thất bại'), 'error');
        }
        
    } catch (error) {
        console.error('Error updating profile:', error);
        showMessage('❌ Lỗi kết nối, vui lòng thử lại', 'error');
    } finally {
        // Restore button state
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalBtnText;
    }
}

/**
 * Xử lý submit form đổi mật khẩu
 */
async function handlePasswordSubmit(e) {
    e.preventDefault();
    
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    
    // Validation
    if (!currentPassword) {
        showMessage('Vui lòng nhập mật khẩu hiện tại', 'error');
        return;
    }
    
    if (newPassword.length < 6) {
        showMessage('Mật khẩu mới phải có ít nhất 6 ký tự', 'error');
        return;
    }
    
    if (newPassword === currentPassword) {
        showMessage('Mật khẩu mới phải khác mật khẩu hiện tại', 'error');
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showMessage('Mật khẩu xác nhận không khớp', 'error');
        return;
    }
    
    // Show loading state
    const submitBtn = document.querySelector('#passwordForm button[type="submit"]');
    const originalBtnText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Đang xử lý...';
    
    try {
        const response = await fetch('/api/profile/change-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                currentPassword,
                newPassword,
                confirmPassword
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            showMessage('✅ Đổi mật khẩu thành công', 'success');
            
            // Reset form
            document.getElementById('passwordForm').reset();
        } else {
            showMessage('❌ ' + (result.message || 'Đổi mật khẩu thất bại'), 'error');
        }
        
    } catch (error) {
        console.error('Error changing password:', error);
        showMessage('❌ Lỗi hệ thống, vui lòng thử lại', 'error');
    } finally {
        // Restore button state
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalBtnText;
    }
}

/**
 * Hiển thị thông báo
 */
function showMessage(message, type = 'info') {
    const container = document.getElementById('message-container');
    
    const bgColor = type === 'success' ? 'bg-green-100 border-green-400 text-green-700' :
                    type === 'error' ? 'bg-red-100 border-red-400 text-red-700' :
                    'bg-blue-100 border-blue-400 text-blue-700';
    
    const icon = type === 'success' ? 'fa-check-circle' :
                 type === 'error' ? 'fa-exclamation-circle' :
                 'fa-info-circle';
    
    container.innerHTML = `
        <div class="border-l-4 ${bgColor} p-4 rounded">
            <div class="flex items-center">
                <i class="fas ${icon} mr-3"></i>
                <p>${message}</p>
            </div>
        </div>
    `;
    
    // Auto hide sau 5 giây
    setTimeout(() => {
        container.innerHTML = '';
    }, 5000);
}

/**
 * Format date
 */
function formatDate(date) {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

/**
 * Kiểm tra trạng thái xác thực email
 */
async function checkEmailVerificationStatus() {
    try {
        const response = await fetch('/api/profile/email-status');
        if (!response.ok) {
            return;
        }
        
        const data = await response.json();
        if (data.success) {
            const badge = document.getElementById('emailVerifiedBadge');
            const resendSection = document.getElementById('resendEmailSection');
            
            if (data.emailVerified) {
                badge.className = 'px-3 py-2 text-sm font-medium bg-green-100 text-green-700 rounded-lg whitespace-nowrap';
                badge.innerHTML = '<i class="fas fa-check-circle mr-1"></i>Đã xác thực';
                resendSection.classList.add('hidden');
            } else {
                badge.className = 'px-3 py-2 text-sm font-medium bg-amber-100 text-amber-700 rounded-lg whitespace-nowrap';
                badge.innerHTML = '<i class="fas fa-exclamation-triangle mr-1"></i>Chưa xác thực';
                resendSection.classList.remove('hidden');
                
                // Setup resend button
                document.getElementById('resendVerificationBtn')?.addEventListener('click', resendVerificationEmail);
            }
        }
    } catch (error) {
        console.error('Error checking email status:', error);
    }
}

/**
 * Gửi lại email xác thực
 */
async function resendVerificationEmail() {
    const btn = document.getElementById('resendVerificationBtn');
    const originalText = btn.innerHTML;
    
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin mr-1"></i>Đang gửi...';
    
    try {
        const response = await fetch('/api/profile/resend-verification', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (data.success) {
            showMessage(data.message, 'success');
            btn.innerHTML = '<i class="fas fa-check mr-1"></i>Đã gửi';
            
            setTimeout(() => {
                btn.innerHTML = originalText;
                btn.disabled = false;
            }, 5000);
        } else {
            showMessage(data.message, 'error');
            btn.innerHTML = originalText;
            btn.disabled = false;
        }
    } catch (error) {
        console.error('Error resending verification email:', error);
        showMessage('Lỗi khi gửi email. Vui lòng thử lại sau.', 'error');
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

/**
 * Setup avatar upload
 */
function setupAvatarUpload() {
    const avatarInput = document.getElementById('avatarInput');
    const avatarUploadBtn = document.getElementById('avatarUploadBtn');
    
    if (avatarUploadBtn && avatarInput) {
        avatarUploadBtn.addEventListener('click', () => avatarInput.click());
        avatarInput.addEventListener('change', handleAvatarUpload);
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
        showMessage('Chỉ chấp nhận file ảnh định dạng JPG, JPEG, PNG', 'error');
        e.target.value = '';
        return;
    }
    
    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
        showMessage('Kích thước file không được vượt quá 5MB', 'error');
        e.target.value = '';
        return;
    }
    
    try {
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
            showMessage('Upload avatar thành công', 'success');
            
            // Update avatar display
            const avatarImg = document.getElementById('avatarImg');
            const avatarPlaceholder = document.getElementById('avatarPlaceholder');
            avatarImg.src = result.avatarUrl;
            avatarImg.classList.remove('hidden');
            avatarPlaceholder.classList.add('hidden');
            
            // Reload profile to update completion
            setTimeout(() => loadProfile(), 500);
        } else {
            showMessage(result.message || 'Upload thất bại', 'error');
        }
        
    } catch (error) {
        console.error('Error uploading avatar:', error);
        showMessage('Lỗi khi upload avatar. Vui lòng thử lại sau.', 'error');
    } finally {
        e.target.value = '';
    }
}
