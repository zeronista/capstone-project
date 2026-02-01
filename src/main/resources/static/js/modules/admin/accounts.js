/**
 * Admin Accounts Management Module
 * Handles account listing, creation, role assignment, and status toggling
 */

// State management
let state = {
    currentUserId: null,
    allAccounts: [],
    currentFilter: ''
};

/**
 * Initialize the accounts page
 */
export function init() {
    loadAccounts();
    setupEventListeners();
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // Search input with debounce
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        let debounceTimer;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                const keyword = e.target.value.trim();
                if (keyword.length >= 2) {
                    searchAccounts(keyword);
                } else if (keyword.length === 0) {
                    loadAccounts();
                }
            }, 300);
        });
    }
    
    // Role filter
    const roleFilter = document.getElementById('roleFilterSelect');
    if (roleFilter) {
        roleFilter.addEventListener('change', (e) => {
            const role = e.target.value.toUpperCase();
            role ? filterByRole(role) : loadAccounts();
        });
    }
    
    // Create account form
    const createForm = document.getElementById('createAccountForm');
    if (createForm) {
        createForm.addEventListener('submit', handleCreateAccount);
    }
}

/**
 * Load all accounts from API
 */
async function loadAccounts() {
    try {
        const response = await fetch('/api/admin/accounts?size=100');
        const data = await response.json();
        
        if (data.success) {
            state.allAccounts = data.accounts;
            renderAccounts(data.accounts);
            updatePagination(data);
        } else {
            showError('Không thể tải danh sách tài khoản');
        }
    } catch (error) {
        console.error('Error loading accounts:', error);
        showError('Lỗi kết nối đến server');
    }
}

/**
 * Search accounts by keyword
 */
async function searchAccounts(keyword) {
    try {
        const response = await fetch(`/api/admin/accounts/search?keyword=${encodeURIComponent(keyword)}`);
        const data = await response.json();
        
        if (data.success) {
            renderAccounts(data.accounts);
        } else {
            showError('Không thể tìm kiếm tài khoản');
        }
    } catch (error) {
        console.error('Error searching accounts:', error);
        showError('Lỗi kết nối đến server');
    }
}

/**
 * Filter accounts by role
 */
async function filterByRole(role) {
    try {
        const response = await fetch(`/api/admin/accounts/filter?role=${role}`);
        const data = await response.json();
        
        if (data.success) {
            renderAccounts(data.accounts);
        } else {
            showError('Không thể lọc tài khoản');
        }
    } catch (error) {
        console.error('Error filtering accounts:', error);
        showError('Lỗi kết nối đến server');
    }
}

/**
 * Render accounts table
 */
function renderAccounts(accounts) {
    const tbody = document.getElementById('accountsTableBody');
    if (!tbody) return;
    
    if (accounts.length === 0) {
        tbody.innerHTML = createEmptyState();
        return;
    }
    
    tbody.innerHTML = accounts.map(createAccountRow).join('');
}

/**
 * Create empty state HTML
 */
function createEmptyState() {
    return `
        <tr>
            <td colspan="7" class="px-6 py-12 text-center">
                <div class="flex flex-col items-center justify-center text-surface-500">
                    <svg class="w-16 h-16 mb-4 text-surface-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
                    </svg>
                    <p class="text-lg font-medium">Không tìm thấy tài khoản nào</p>
                    <p class="text-sm mt-1">Thử thay đổi bộ lọc hoặc tìm kiếm với từ khóa khác</p>
                </div>
            </td>
        </tr>
    `;
}

/**
 * Create account row HTML
 */
function createAccountRow(account) {
    const initials = getInitials(account.fullName);
    const roleInfo = getRoleInfo(account.role);
    const statusInfo = getStatusInfo(account.enabled);
    const createdDate = formatDate(account.createdAt);
    
    return `
        <tr class="hover:bg-surface-50 transition-colors duration-150">
            <td class="px-6 py-4">
                <input type="checkbox" class="w-4 h-4 rounded border-surface-300 text-primary-500 focus:ring-primary-500 cursor-pointer">
            </td>
            <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-gradient-to-br ${roleInfo.gradient} rounded-full flex items-center justify-center">
                        <span class="text-white font-semibold text-sm">${initials}</span>
                    </div>
                    <div>
                        <p class="font-medium text-surface-900">${account.fullName}</p>
                        <p class="text-sm text-surface-500">${account.email || account.phone || '-'}</p>
                    </div>
                </div>
            </td>
            <td class="px-6 py-4">
                <span class="px-3 py-1 ${roleInfo.bgColor} ${roleInfo.textColor} text-xs font-medium rounded-full">${roleInfo.label}</span>
            </td>
            <td class="px-6 py-4">
                <span class="inline-flex items-center gap-1.5 px-2.5 py-1 ${statusInfo.bgColor} ${statusInfo.textColor} text-xs font-medium rounded-full">
                    <span class="w-1.5 h-1.5 ${statusInfo.dotColor} rounded-full"></span>
                    ${statusInfo.label}
                </span>
            </td>
            <td class="px-6 py-4 text-sm text-surface-600">-</td>
            <td class="px-6 py-4 text-sm text-surface-600">${createdDate}</td>
            <td class="px-6 py-4">
                <div class="flex items-center justify-end gap-2">
                    <button onclick="window.adminAccounts.openAssignRoleModal(${account.id}, '${account.fullName}', '${account.role}')" 
                            class="p-2 text-surface-400 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors duration-200 cursor-pointer" 
                            title="Phân quyền">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
                        </svg>
                    </button>
                    <button onclick="window.adminAccounts.toggleAccountStatus(${account.id})" 
                            class="p-2 text-surface-400 hover:text-${account.enabled ? 'red' : 'secondary'}-600 hover:bg-${account.enabled ? 'red' : 'secondary'}-50 rounded-lg transition-colors duration-200 cursor-pointer" 
                            title="${account.enabled ? 'Vô hiệu hóa' : 'Kích hoạt'}">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="${account.enabled ? 'M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636' : 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'}"/>
                        </svg>
                    </button>
                </div>
            </td>
        </tr>
    `;
}

/**
 * Handle create account form submission
 */
async function handleCreateAccount(e) {
    e.preventDefault();
    
    // Clear previous errors
    clearFieldErrors();
    
    // Get form data
    const formData = {
        fullName: document.getElementById('createFullName').value.trim(),
        email: document.getElementById('createEmail').value.trim(),
        phone: document.getElementById('createPhone').value.trim(),
        role: document.getElementById('createRole').value,
        password: document.getElementById('createPassword').value,
        confirmPassword: document.getElementById('createConfirmPassword').value
    };
    
    // Client-side validation
    if (!validateCreateForm(formData)) {
        return;
    }
    
    // Call API
    try {
        const response = await fetch('/api/admin/accounts', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                ...formData,
                email: formData.email || null,
                phone: formData.phone || null
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showSuccess(data.message || 'Tạo tài khoản thành công');
            closeCreateAccountModal();
            loadAccounts();
        } else {
            showError(data.message || 'Không thể tạo tài khoản');
        }
    } catch (error) {
        console.error('Error creating account:', error);
        showError('Lỗi kết nối đến server');
    }
}

/**
 * Validate create form
 */
function validateCreateForm(data) {
    let isValid = true;
    
    if (!data.fullName || data.fullName.length < 2) {
        showFieldError('errorFullName', 'Họ và tên phải có ít nhất 2 ký tự');
        isValid = false;
    }
    
    if (!data.email && !data.phone) {
        showFieldError('errorEmail', 'Vui lòng cung cấp email hoặc số điện thoại');
        showFieldError('errorPhone', 'Vui lòng cung cấp email hoặc số điện thoại');
        isValid = false;
    }
    
    if (data.email && !isValidEmail(data.email)) {
        showFieldError('errorEmail', 'Email không hợp lệ');
        isValid = false;
    }
    
    if (data.phone && !isValidPhone(data.phone)) {
        showFieldError('errorPhone', 'Số điện thoại không hợp lệ (VD: 0901234567)');
        isValid = false;
    }
    
    if (!data.role) {
        showFieldError('errorRole', 'Vui lòng chọn vai trò');
        isValid = false;
    }
    
    if (!data.password || data.password.length < 8) {
        showFieldError('errorPassword', 'Mật khẩu phải có ít nhất 8 ký tự');
        isValid = false;
    } else if (!isValidPassword(data.password)) {
        showFieldError('errorPassword', 'Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa và 1 số');
        isValid = false;
    }
    
    if (data.password !== data.confirmPassword) {
        showFieldError('errorConfirmPassword', 'Mật khẩu xác nhận không khớp');
        isValid = false;
    }
    
    return isValid;
}

/**
 * Open assign role modal
 */
export function openAssignRoleModal(userId, userName, currentRole) {
    state.currentUserId = userId;
    document.getElementById('modalUserName').textContent = userName;
    document.getElementById('newRoleSelect').value = '';
    document.getElementById('assignRoleModal').classList.remove('hidden');
}

/**
 * Close assign role modal
 */
export function closeAssignRoleModal() {
    state.currentUserId = null;
    document.getElementById('assignRoleModal').classList.add('hidden');
}

/**
 * Open create account modal
 */
export function openCreateAccountModal() {
    document.getElementById('createAccountForm').reset();
    clearFieldErrors();
    document.getElementById('createAccountModal').classList.remove('hidden');
}

/**
 * Close create account modal
 */
export function closeCreateAccountModal() {
    document.getElementById('createAccountModal').classList.add('hidden');
}

/**
 * Confirm assign role
 */
export async function confirmAssignRole() {
    const newRole = document.getElementById('newRoleSelect').value;
    
    if (!newRole) {
        showError('Vui lòng chọn vai trò');
        return;
    }
    
    if (!state.currentUserId) {
        showError('Lỗi: Không xác định được user');
        return;
    }
    
    try {
        const response = await fetch('/api/admin/accounts/assign-role', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: state.currentUserId,
                role: newRole
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showSuccess(data.message || 'Cập nhật vai trò thành công');
            closeAssignRoleModal();
            loadAccounts();
        } else {
            showError(data.message || 'Không thể cập nhật vai trò');
        }
    } catch (error) {
        console.error('Error assigning role:', error);
        showError('Lỗi kết nối đến server');
    }
}

/**
 * Toggle account status
 */
export async function toggleAccountStatus(userId) {
    if (!confirm('Bạn có chắc muốn thay đổi trạng thái tài khoản này?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/admin/accounts/${userId}/toggle-status`, {
            method: 'PUT'
        });
        
        const data = await response.json();
        
        if (data.success) {
            showSuccess(data.message);
            loadAccounts();
        } else {
            showError(data.message || 'Không thể cập nhật trạng thái');
        }
    } catch (error) {
        console.error('Error toggling account status:', error);
        showError('Lỗi kết nối đến server');
    }
}

// Utility functions
function getInitials(fullName) {
    if (!fullName) return '??';
    const parts = fullName.trim().split(' ');
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function getRoleInfo(role) {
    const roleMap = {
        'ADMIN': { label: 'Admin', bgColor: 'bg-purple-100', textColor: 'text-purple-700', gradient: 'from-purple-400 to-purple-600' },
        'DOCTOR': { label: 'Bác sĩ', bgColor: 'bg-primary-100', textColor: 'text-primary-700', gradient: 'from-primary-400 to-primary-600' },
        'NURSE': { label: 'Y tá', bgColor: 'bg-amber-100', textColor: 'text-amber-700', gradient: 'from-amber-400 to-amber-600' },
        'STAFF': { label: 'Nhân viên', bgColor: 'bg-blue-100', textColor: 'text-blue-700', gradient: 'from-blue-400 to-blue-600' },
        'PATIENT': { label: 'Bệnh nhân', bgColor: 'bg-surface-100', textColor: 'text-surface-700', gradient: 'from-surface-400 to-surface-600' },
        'RECEPTIONIST': { label: 'Lễ tân', bgColor: 'bg-pink-100', textColor: 'text-pink-700', gradient: 'from-pink-400 to-pink-600' }
    };
    return roleMap[role] || roleMap['PATIENT'];
}

function getStatusInfo(enabled) {
    return enabled 
        ? { label: 'Hoạt động', bgColor: 'bg-secondary-100', textColor: 'text-secondary-700', dotColor: 'bg-secondary-500' }
        : { label: 'Vô hiệu hóa', bgColor: 'bg-surface-100', textColor: 'text-surface-600', dotColor: 'bg-surface-400' };
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

function updatePagination(data) {
    const paginationText = document.querySelector('.text-sm.text-surface-500');
    if (paginationText && data.totalItems) {
        const showing = data.accounts.length;
        paginationText.textContent = `Hiển thị ${showing} của ${data.totalItems} tài khoản`;
    }
}

function clearFieldErrors() {
    document.querySelectorAll('[id^="error"]').forEach(el => {
        el.textContent = '';
        el.classList.add('hidden');
    });
}

function showFieldError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.classList.remove('hidden');
    }
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidPhone(phone) {
    return /^(0[3|5|7|8|9])+([0-9]{8})$/.test(phone);
}

function isValidPassword(password) {
    return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/.test(password);
}

function showSuccess(message) {
    // TODO: Replace with better notification system
    alert(message);
}

function showError(message) {
    // TODO: Replace with better notification system
    alert('Lỗi: ' + message);
}
