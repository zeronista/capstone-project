/**
 * API Client Module
 * 
 * Centralized API client for making HTTP requests to the backend.
 * Provides standardized error handling, request/response interceptors,
 * and type documentation via JSDoc.
 * 
 * @version 1.0.0
 * @date January 28, 2026
 * 
 * @example
 * // Usage example:
 * const api = new ApiClient('/api');
 * 
 * // GET request
 * const data = await api.get('/patient/stats');
 * 
 * // POST request
 * const result = await api.post('/profile/update', { name: 'John Doe' });
 * 
 * // With custom headers
 * const data = await api.get('/protected/resource', {
 *     headers: { 'X-Custom-Header': 'value' }
 * });
 */

/**
 * @typedef {Object} ApiResponse
 * @property {boolean} success - Whether the request was successful
 * @property {*} data - Response data
 * @property {string} [message] - Optional message
 * @property {number} status - HTTP status code
 */

/**
 * @typedef {Object} ApiError
 * @property {string} message - Error message
 * @property {number} status - HTTP status code
 * @property {*} [data] - Optional error data from server
 */

/**
 * @typedef {Object} RequestOptions
 * @property {Object} [headers] - Additional request headers
 * @property {AbortSignal} [signal] - AbortSignal for request cancellation
 * @property {number} [timeout] - Request timeout in milliseconds
 */

class ApiClient {
    /**
     * Create an API client instance
     * @param {string} baseURL - Base URL for all API requests (e.g., '/api')
     * @param {Object} [defaultOptions] - Default options for all requests
     * @param {Object} [defaultOptions.headers] - Default headers
     * @param {number} [defaultOptions.timeout=30000] - Default timeout in ms
     */
    constructor(baseURL = '', defaultOptions = {}) {
        this.baseURL = baseURL;
        this.defaultOptions = {
            timeout: 30000,
            headers: {
                'Content-Type': 'application/json',
            },
            ...defaultOptions
        };
        
        // Request interceptors (functions to run before each request)
        this.requestInterceptors = [];
        
        // Response interceptors (functions to run after each response)
        this.responseInterceptors = [];
    }

    /**
     * Add a request interceptor
     * @param {Function} interceptor - Function that receives (url, options) and returns modified options
     */
    addRequestInterceptor(interceptor) {
        this.requestInterceptors.push(interceptor);
    }

    /**
     * Add a response interceptor
     * @param {Function} interceptor - Function that receives (response) and returns modified response
     */
    addResponseInterceptor(interceptor) {
        this.responseInterceptors.push(interceptor);
    }

    /**
     * Build complete URL from relative path
     * @private
     * @param {string} endpoint - API endpoint
     * @returns {string} Complete URL
     */
    _buildURL(endpoint) {
        // Remove leading slash from endpoint if baseURL ends with slash
        const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint;
        const cleanBase = this.baseURL.endsWith('/') ? this.baseURL.slice(0, -1) : this.baseURL;
        return `${cleanBase}/${cleanEndpoint}`;
    }

    /**
     * Merge default options with request options
     * @private
     * @param {RequestOptions} options - Request-specific options
     * @returns {Object} Merged options
     */
    _mergeOptions(options = {}) {
        return {
            ...this.defaultOptions,
            ...options,
            headers: {
                ...this.defaultOptions.headers,
                ...options.headers
            }
        };
    }

    /**
     * Apply request interceptors
     * @private
     * @param {string} url - Request URL
     * @param {Object} options - Fetch options
     * @returns {Object} Modified options
     */
    async _applyRequestInterceptors(url, options) {
        let modifiedOptions = { ...options };
        
        for (const interceptor of this.requestInterceptors) {
            try {
                modifiedOptions = await interceptor(url, modifiedOptions) || modifiedOptions;
            } catch (error) {
                console.error('Request interceptor error:', error);
            }
        }
        
        return modifiedOptions;
    }

    /**
     * Apply response interceptors
     * @private
     * @param {Response} response - Fetch response
     * @returns {Response} Modified response
     */
    async _applyResponseInterceptors(response) {
        let modifiedResponse = response;
        
        for (const interceptor of this.responseInterceptors) {
            try {
                modifiedResponse = await interceptor(modifiedResponse) || modifiedResponse;
            } catch (error) {
                console.error('Response interceptor error:', error);
            }
        }
        
        return modifiedResponse;
    }

    /**
     * Handle fetch response
     * @private
     * @param {Response} response - Fetch response
     * @returns {Promise<*>} Parsed response data
     * @throws {ApiError} If response is not ok
     */
    async _handleResponse(response) {
        // Apply response interceptors
        response = await this._applyResponseInterceptors(response);
        
        const contentType = response.headers.get('content-type');
        let data;
        
        // Parse response based on content type
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else if (contentType && contentType.includes('text/')) {
            data = await response.text();
        } else {
            data = await response.blob();
        }
        
        // Check if response is successful
        if (!response.ok) {
            const error = new Error(data?.message || `HTTP ${response.status}: ${response.statusText}`);
            error.status = response.status;
            error.data = data;
            throw error;
        }
        
        return data;
    }

    /**
     * Handle request errors
     * @private
     * @param {Error} error - Error object
     * @throws {ApiError} Formatted error
     */
    _handleError(error) {
        if (error.name === 'AbortError') {
            const abortError = new Error('Request was cancelled');
            abortError.status = 0;
            abortError.code = 'ABORTED';
            throw abortError;
        }
        
        if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
            const networkError = new Error('Network error: Unable to connect to server');
            networkError.status = 0;
            networkError.code = 'NETWORK_ERROR';
            throw networkError;
        }
        
        // Re-throw other errors
        throw error;
    }

    /**
     * Make an HTTP request
     * @private
     * @param {string} endpoint - API endpoint
     * @param {string} method - HTTP method
     * @param {Object} [options] - Request options
     * @returns {Promise<*>} Response data
     */
    async _request(endpoint, method, options = {}) {
        const url = this._buildURL(endpoint);
        const mergedOptions = this._mergeOptions(options);
        
        // Setup abort controller for timeout
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), mergedOptions.timeout);
        
        let fetchOptions = {
            method,
            headers: mergedOptions.headers,
            signal: options.signal || controller.signal,
        };
        
        // Add body for non-GET requests
        if (options.body !== undefined) {
            if (mergedOptions.headers['Content-Type'] === 'application/json') {
                fetchOptions.body = JSON.stringify(options.body);
            } else {
                fetchOptions.body = options.body;
            }
        }
        
        try {
            // Apply request interceptors
            fetchOptions = await this._applyRequestInterceptors(url, fetchOptions);
            
            // Make the request
            const response = await fetch(url, fetchOptions);
            clearTimeout(timeoutId);
            
            return await this._handleResponse(response);
        } catch (error) {
            clearTimeout(timeoutId);
            this._handleError(error);
        }
    }

    /**
     * Make a GET request
     * @param {string} endpoint - API endpoint
     * @param {RequestOptions} [options] - Request options
     * @returns {Promise<*>} Response data
     * 
     * @example
     * const stats = await api.get('/patient/stats');
     * const user = await api.get('/users/123');
     */
    async get(endpoint, options = {}) {
        return this._request(endpoint, 'GET', options);
    }

    /**
     * Make a POST request
     * @param {string} endpoint - API endpoint
     * @param {*} body - Request body
     * @param {RequestOptions} [options] - Request options
     * @returns {Promise<*>} Response data
     * 
     * @example
     * const result = await api.post('/profile/update', {
     *     name: 'John Doe',
     *     email: 'john@example.com'
     * });
     */
    async post(endpoint, body, options = {}) {
        return this._request(endpoint, 'POST', { ...options, body });
    }

    /**
     * Make a PUT request
     * @param {string} endpoint - API endpoint
     * @param {*} body - Request body
     * @param {RequestOptions} [options] - Request options
     * @returns {Promise<*>} Response data
     * 
     * @example
     * const updated = await api.put('/users/123', { name: 'Jane Doe' });
     */
    async put(endpoint, body, options = {}) {
        return this._request(endpoint, 'PUT', { ...options, body });
    }

    /**
     * Make a PATCH request
     * @param {string} endpoint - API endpoint
     * @param {*} body - Request body
     * @param {RequestOptions} [options] - Request options
     * @returns {Promise<*>} Response data
     * 
     * @example
     * const patched = await api.patch('/users/123', { email: 'new@example.com' });
     */
    async patch(endpoint, body, options = {}) {
        return this._request(endpoint, 'PATCH', { ...options, body });
    }

    /**
     * Make a DELETE request
     * @param {string} endpoint - API endpoint
     * @param {RequestOptions} [options] - Request options
     * @returns {Promise<*>} Response data
     * 
     * @example
     * await api.delete('/users/123');
     */
    async delete(endpoint, options = {}) {
        return this._request(endpoint, 'DELETE', options);
    }

    /**
     * Upload a file with FormData
     * @param {string} endpoint - API endpoint
     * @param {FormData} formData - FormData object with file
     * @param {Function} [onProgress] - Progress callback (receives percentage)
     * @param {RequestOptions} [options] - Request options
     * @returns {Promise<*>} Response data
     * 
     * @example
     * const formData = new FormData();
     * formData.append('file', fileInput.files[0]);
     * formData.append('description', 'Profile picture');
     * 
     * const result = await api.upload('/profile/upload-avatar', formData, (percent) => {
     *     console.log(`Upload progress: ${percent}%`);
     * });
     */
    async upload(endpoint, formData, onProgress, options = {}) {
        const url = this._buildURL(endpoint);
        
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            
            // Progress tracking
            if (onProgress && typeof onProgress === 'function') {
                xhr.upload.addEventListener('progress', (e) => {
                    if (e.lengthComputable) {
                        const percentComplete = (e.loaded / e.total) * 100;
                        onProgress(Math.round(percentComplete));
                    }
                });
            }
            
            // Handle completion
            xhr.addEventListener('load', () => {
                if (xhr.status >= 200 && xhr.status < 300) {
                    try {
                        const data = JSON.parse(xhr.responseText);
                        resolve(data);
                    } catch (e) {
                        resolve(xhr.responseText);
                    }
                } else {
                    reject(new Error(`Upload failed with status ${xhr.status}`));
                }
            });
            
            // Handle errors
            xhr.addEventListener('error', () => {
                reject(new Error('Upload failed: Network error'));
            });
            
            xhr.addEventListener('abort', () => {
                reject(new Error('Upload was cancelled'));
            });
            
            // Open and send
            xhr.open('POST', url);
            
            // Add custom headers (but not Content-Type for FormData)
            if (options.headers) {
                Object.entries(options.headers).forEach(([key, value]) => {
                    if (key.toLowerCase() !== 'content-type') {
                        xhr.setRequestHeader(key, value);
                    }
                });
            }
            
            xhr.send(formData);
        });
    }
}

// ============================================================================
// Pre-configured API Client Instances
// ============================================================================

/**
 * Main API client for general API calls
 * @type {ApiClient}
 */
const apiClient = new ApiClient('/api');

/**
 * Add CSRF token to all requests (if available)
 * This interceptor adds the CSRF token from meta tag to request headers
 */
apiClient.addRequestInterceptor((url, options) => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    
    if (csrfToken && csrfHeader) {
        options.headers[csrfHeader] = csrfToken;
    }
    
    return options;
});

/**
 * Log all requests in development mode
 * Remove or comment out in production
 */
if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    apiClient.addRequestInterceptor((url, options) => {
        console.log(`[API] ${options.method} ${url}`, options);
        return options;
    });
    
    apiClient.addResponseInterceptor((response) => {
        console.log(`[API] Response ${response.status}`, response);
        return response;
    });
}

// ============================================================================
// Convenience Functions for Common API Patterns
// ============================================================================

/**
 * API helper functions for common endpoints
 */
const API = {
    // Patient endpoints
    patient: {
        getStats: () => apiClient.get('/patient/stats'),
        getPrescriptions: () => apiClient.get('/patient/prescriptions'),
        getTreatments: () => apiClient.get('/patient/treatments'),
        getTickets: () => apiClient.get('/patient/tickets'),
        getProfile: () => apiClient.get('/patient/profile'),
    },
    
    // Profile endpoints
    profile: {
        get: () => apiClient.get('/profile'),
        update: (data) => apiClient.put('/profile/update', data),
        uploadAvatar: (formData, onProgress) => apiClient.upload('/profile/upload-avatar', formData, onProgress),
    },
    
    // Admin endpoints
    admin: {
        getAccounts: (params = {}) => {
            const query = new URLSearchParams(params).toString();
            return apiClient.get(`/admin/accounts${query ? '?' + query : ''}`);
        },
        searchAccounts: (keyword) => apiClient.get(`/admin/accounts/search?keyword=${encodeURIComponent(keyword)}`),
        filterAccounts: (role) => apiClient.get(`/admin/accounts/filter?role=${role}`),
    },
    
    // Stringee / Call endpoints
    stringee: {
        uploadRecording: (data) => apiClient.post('/stringee/upload-recording', data),
    },
};

// Export for use in modules (if using ES6 modules)
// export { ApiClient, apiClient, API };

// Make available globally (for non-module scripts)
window.ApiClient = ApiClient;
window.apiClient = apiClient;
window.API = API;
