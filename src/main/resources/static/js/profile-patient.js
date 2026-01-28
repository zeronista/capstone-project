/**
 * JavaScript cho trang Profile của bệnh nhân
 */

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
        const response = await fetch('/api/patient/profile');
        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = '/auth/login';
                return;
            }
            throw new Error('Không thể tải thông tin profile');
        }

        const data = await response.json();
        
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
        dateOfBirth: document.getElementById('dateOfBirth').value,
        gender: document.getElementById('gender').value,
        address: document.getElementById('address').value.trim()
    };
    
    // Validation
    if (!formData.fullName) {
        showMessage('Vui lòng nhập họ tên', 'error');
        return;
    }
    
    if (!formData.phoneNumber) {
        showMessage('Vui lòng nhập số điện thoại', 'error');
        return;
    }
    
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
            showMessage('Cập nhật thông tin thành công', 'success');
            
            // Cập nhật dữ liệu gốc
            originalProfile = { ...originalProfile, ...formData };
            
            // Thoát chế độ chỉnh sửa
            cancelEditing();
            
            // Reload để cập nhật header
            setTimeout(() => loadProfile(), 500);
        } else {
            showMessage(result.message || 'Cập nhật thất bại', 'error');
        }
        
    } catch (error) {
        console.error('Error updating profile:', error);
        showMessage('Lỗi hệ thống, vui lòng thử lại', 'error');
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
    if (newPassword.length < 6) {
        showMessage('Mật khẩu mới phải có ít nhất 6 ký tự', 'error');
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showMessage('Mật khẩu xác nhận không khớp', 'error');
        return;
    }
    
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
            showMessage('Đổi mật khẩu thành công', 'success');
            
            // Reset form
            document.getElementById('passwordForm').reset();
        } else {
            showMessage(result.message || 'Đổi mật khẩu thất bại', 'error');
        }
        
    } catch (error) {
        console.error('Error changing password:', error);
        showMessage('Lỗi hệ thống, vui lòng thử lại', 'error');
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
