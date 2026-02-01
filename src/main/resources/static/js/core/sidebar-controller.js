/**
 * ABClinic Unified Sidebar Controller
 * 
 * Handles sidebar toggle functionality for mobile and desktop views.
 * Supports collapsible sidebar with localStorage persistence.
 * 
 * @version 2.0.0
 * @date January 31, 2026
 */

(function() {
  'use strict';

  // DOM Elements
  const sidebar = document.querySelector('[data-sidebar]');
  const backdrop = document.querySelector('[data-sidebar-backdrop]');
  const mobileToggle = document.querySelector('[data-sidebar-toggle]');
  const desktopToggle = document.querySelector('[data-sidebar-desktop-toggle]');

  // State
  const STORAGE_KEY = 'abclinic_sidebar_collapsed';
  let isCollapsed = localStorage.getItem(STORAGE_KEY) === 'true';
  let isMobileOpen = false;

  /**
   * Initialize sidebar state
   */
  function init() {
    if (!sidebar) return;

    // Apply initial collapsed state on desktop
    if (window.innerWidth >= 1024 && isCollapsed) {
      sidebar.classList.add('sidebar-collapsed');
    }

    // Setup event listeners
    setupEventListeners();

    // Apply initial ARIA states
    updateAriaStates();
  }

  /**
   * Setup all event listeners
   */
  function setupEventListeners() {
    // Mobile toggle
    if (mobileToggle) {
      mobileToggle.addEventListener('click', toggleMobileSidebar);
    }

    // Desktop toggle
    if (desktopToggle) {
      desktopToggle.addEventListener('click', toggleDesktopSidebar);
    }

    // Backdrop click closes mobile sidebar
    if (backdrop) {
      backdrop.addEventListener('click', closeMobileSidebar);
    }

    // Keyboard shortcut (Ctrl+B)
    document.addEventListener('keydown', function(e) {
      if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
        e.preventDefault();
        if (window.innerWidth >= 1024) {
          toggleDesktopSidebar();
        } else {
          toggleMobileSidebar();
        }
      }
    });

    // Handle window resize
    window.addEventListener('resize', handleResize);

    // Close mobile sidebar on escape key
    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape' && isMobileOpen) {
        closeMobileSidebar();
      }
    });
  }

  /**
   * Toggle mobile sidebar
   */
  function toggleMobileSidebar() {
    isMobileOpen = !isMobileOpen;
    
    if (isMobileOpen) {
      openMobileSidebar();
    } else {
      closeMobileSidebar();
    }
  }

  /**
   * Open mobile sidebar
   */
  function openMobileSidebar() {
    if (!sidebar || !backdrop) return;
    
    isMobileOpen = true;
    sidebar.classList.remove('sidebar-hidden');
    sidebar.classList.add('sidebar-open');
    backdrop.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    
    updateAriaStates();
  }

  /**
   * Close mobile sidebar
   */
  function closeMobileSidebar() {
    if (!sidebar || !backdrop) return;
    
    isMobileOpen = false;
    sidebar.classList.add('sidebar-hidden');
    sidebar.classList.remove('sidebar-open');
    backdrop.classList.add('hidden');
    document.body.style.overflow = '';
    
    updateAriaStates();
  }

  /**
   * Toggle desktop sidebar collapse
   */
  function toggleDesktopSidebar() {
    if (!sidebar) return;
    
    isCollapsed = !isCollapsed;
    sidebar.classList.toggle('sidebar-collapsed', isCollapsed);
    localStorage.setItem(STORAGE_KEY, isCollapsed);
    
    updateAriaStates();
  }

  /**
   * Handle window resize
   */
  function handleResize() {
    // Close mobile sidebar when resizing to desktop
    if (window.innerWidth >= 1024 && isMobileOpen) {
      closeMobileSidebar();
    }
  }

  /**
   * Update ARIA states for accessibility
   */
  function updateAriaStates() {
    if (mobileToggle) {
      mobileToggle.setAttribute('aria-expanded', isMobileOpen);
    }
    if (desktopToggle) {
      desktopToggle.setAttribute('aria-expanded', !isCollapsed);
    }
    if (sidebar) {
      sidebar.setAttribute('aria-hidden', window.innerWidth < 1024 && !isMobileOpen);
    }
  }

  // Initialize on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  // Expose functions globally for inline onclick handlers
  window.toggleMobileSidebar = toggleMobileSidebar;
  window.closeMobileSidebar = closeMobileSidebar;
  window.toggleDesktopSidebar = toggleDesktopSidebar;
})();
