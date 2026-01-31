/**
 * Doctor Sidebar Controller
 * Manages sidebar toggle functionality, responsive behavior, and state persistence
 * 
 * Features:
 * - Desktop: Icon-only collapsed mode (4rem width)
 * - Mobile: Drawer overlay with backdrop
 * - localStorage persistence
 * - Keyboard shortcut: Ctrl+B
 * - ARIA accessibility support
 * - Performance optimized with will-change
 * 
 * @version 1.0.0
 * @date January 31, 2026
 */

class DoctorSidebarController {
    constructor() {
        this.sidebar = null;
        this.backdrop = null;
        this.toggleBtn = null;
        this.desktopToggleBtn = null;
        this.storageKey = 'doctor-sidebar-open';
        this.isDesktop = window.innerWidth >= 1024; // lg breakpoint
        this.transitionDuration = 300; // ms
        
        this.init();
    }

    init() {
        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setup());
        } else {
            this.setup();
        }
    }

    setup() {
        // Get DOM elements
        this.sidebar = document.querySelector('[data-sidebar]');
        this.backdrop = document.querySelector('[data-sidebar-backdrop]');
        this.toggleBtn = document.querySelector('[data-sidebar-toggle]');
        this.desktopToggleBtn = document.querySelector('[data-sidebar-desktop-toggle]');

        if (!this.sidebar) {
            console.warn('Doctor sidebar not found');
            return;
        }

        // Initialize responsive behavior
        this.updateResponsiveState();
        
        // Load saved state from localStorage
        this.loadState();

        // Setup event listeners
        this.attachEventListeners();

        // Update ARIA attributes
        this.updateAriaAttributes();
    }

    updateResponsiveState() {
        this.isDesktop = window.innerWidth >= 1024;
    }

    loadState() {
        const savedState = localStorage.getItem(this.storageKey);
        
        if (this.isDesktop) {
            // Desktop: default open, can be collapsed to icon-only
            const isOpen = savedState !== 'false';
            if (!isOpen) {
                this.sidebar.classList.add('sidebar-collapsed');
            }
        } else {
            // Mobile: default closed (drawer hidden)
            const isOpen = savedState === 'true';
            if (!isOpen) {
                this.sidebar.classList.add('sidebar-hidden');
            }
        }
    }

    attachEventListeners() {
        // Toggle button click
        if (this.toggleBtn) {
            this.toggleBtn.addEventListener('click', () => this.toggleSidebar());
        }

        if (this.desktopToggleBtn) {
            this.desktopToggleBtn.addEventListener('click', () => this.toggleSidebar());
        }

        // Backdrop click (mobile only)
        if (this.backdrop) {
            this.backdrop.addEventListener('click', () => this.closeSidebar());
        }

        // Click outside to close (mobile only)
        document.addEventListener('click', (e) => this.handleClickOutside(e));

        // Keyboard shortcut: Ctrl+B
        document.addEventListener('keydown', (e) => this.handleKeyboardShortcut(e));

        // Window resize
        window.addEventListener('resize', () => this.handleResize());

        // Prevent clicks inside sidebar from closing it
        if (this.sidebar) {
            this.sidebar.addEventListener('click', (e) => e.stopPropagation());
        }
    }

    toggleSidebar() {
        const isOpen = this.isOpen();
        
        if (isOpen) {
            this.closeSidebar();
        } else {
            this.openSidebar();
        }
    }

    openSidebar() {
        if (!this.sidebar) return;

        // Add will-change for performance
        this.sidebar.style.willChange = 'width, transform';

        // Open sidebar
        this.sidebar.classList.remove('sidebar-collapsed', 'sidebar-hidden');
        
        // Show backdrop on mobile
        if (!this.isDesktop && this.backdrop) {
            this.backdrop.classList.remove('hidden');
        }

        // Save state
        localStorage.setItem(this.storageKey, 'true');

        // Update ARIA
        this.updateAriaAttributes();

        // Remove will-change after transition
        setTimeout(() => {
            if (this.sidebar) {
                this.sidebar.style.willChange = 'auto';
            }
        }, this.transitionDuration);
    }

    closeSidebar() {
        if (!this.sidebar) return;

        // Add will-change for performance
        this.sidebar.style.willChange = 'width, transform';

        if (this.isDesktop) {
            // Desktop: collapse to icon-only
            this.sidebar.classList.add('sidebar-collapsed');
        } else {
            // Mobile: hide completely
            this.sidebar.classList.add('sidebar-hidden');
            
            // Hide backdrop
            if (this.backdrop) {
                this.backdrop.classList.add('hidden');
            }
        }

        // Save state
        localStorage.setItem(this.storageKey, 'false');

        // Update ARIA
        this.updateAriaAttributes();

        // Remove will-change after transition
        setTimeout(() => {
            if (this.sidebar) {
                this.sidebar.style.willChange = 'auto';
            }
        }, this.transitionDuration);
    }

    isOpen() {
        if (!this.sidebar) return false;
        
        if (this.isDesktop) {
            return !this.sidebar.classList.contains('sidebar-collapsed');
        } else {
            return !this.sidebar.classList.contains('sidebar-hidden');
        }
    }

    handleClickOutside(e) {
        // Only on mobile
        if (this.isDesktop) return;

        // Only if sidebar is open
        if (!this.isOpen()) return;

        // Check if click is outside sidebar and toggle button
        const clickedSidebar = this.sidebar && this.sidebar.contains(e.target);
        const clickedToggle = this.toggleBtn && this.toggleBtn.contains(e.target);
        const clickedDesktopToggle = this.desktopToggleBtn && this.desktopToggleBtn.contains(e.target);

        if (!clickedSidebar && !clickedToggle && !clickedDesktopToggle) {
            this.closeSidebar();
        }
    }

    handleKeyboardShortcut(e) {
        // Ctrl+B to toggle sidebar
        if (e.ctrlKey && e.key === 'b') {
            e.preventDefault();
            this.toggleSidebar();
        }
    }

    handleResize() {
        const wasDesktop = this.isDesktop;
        this.updateResponsiveState();

        // If switched between mobile and desktop
        if (wasDesktop !== this.isDesktop) {
            // Reset classes based on new breakpoint
            this.sidebar.classList.remove('sidebar-collapsed', 'sidebar-hidden');
            
            // Reload state for new breakpoint
            this.loadState();
            
            // Update ARIA
            this.updateAriaAttributes();
        }
    }

    updateAriaAttributes() {
        const isOpen = this.isOpen();

        // Update toggle buttons
        if (this.toggleBtn) {
            this.toggleBtn.setAttribute('aria-expanded', isOpen);
            this.toggleBtn.setAttribute('aria-label', isOpen ? 'Đóng menu' : 'Mở menu');
        }

        if (this.desktopToggleBtn) {
            this.desktopToggleBtn.setAttribute('aria-expanded', isOpen);
            this.desktopToggleBtn.setAttribute('aria-label', isOpen ? 'Thu gọn menu' : 'Mở rộng menu');
        }

        // Update sidebar
        if (this.sidebar) {
            this.sidebar.setAttribute('aria-hidden', !isOpen);
        }
    }
}

// Initialize controller
const sidebarController = new DoctorSidebarController();

// Export for programmatic control
export default sidebarController;
