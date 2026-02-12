/**
 * ABClinic Unified Theme Controller
 * 
 * Handles dark/light mode synchronization across all pages.
 * Uses localStorage with key "theme" and values "dark" or "light".
 * 
 * @version 1.0.0
 * @date February 2, 2026
 */

(function() {
  'use strict';

  const THEME_KEY = 'theme';
  const DARK_MODE_KEY = 'darkMode'; // Legacy key for migration

  /**
   * Initialize theme on page load
   * This should be called as early as possible to prevent flash
   */
  function initTheme() {
    // Migrate from old darkMode key if exists
    migrateOldKey();
    
    const theme = localStorage.getItem(THEME_KEY);
    
    if (theme === 'dark' || 
        (!theme && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  /**
   * Migrate from old "darkMode" key to "theme" key
   */
  function migrateOldKey() {
    const oldValue = localStorage.getItem(DARK_MODE_KEY);
    if (oldValue !== null) {
      // Convert "true"/"false" to "dark"/"light"
      const newValue = oldValue === 'true' ? 'dark' : 'light';
      localStorage.setItem(THEME_KEY, newValue);
      localStorage.removeItem(DARK_MODE_KEY);
    }
  }

  /**
   * Toggle between dark and light mode
   */
  function toggleTheme() {
    const isDark = document.documentElement.classList.toggle('dark');
    localStorage.setItem(THEME_KEY, isDark ? 'dark' : 'light');
    
    // Dispatch custom event for other components to listen
    window.dispatchEvent(new CustomEvent('themeChanged', { 
      detail: { theme: isDark ? 'dark' : 'light' }
    }));
    
    return isDark;
  }

  /**
   * Set specific theme
   * @param {string} theme - 'dark' or 'light'
   */
  function setTheme(theme) {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
    localStorage.setItem(THEME_KEY, theme);
    
    window.dispatchEvent(new CustomEvent('themeChanged', { 
      detail: { theme }
    }));
  }

  /**
   * Get current theme
   * @returns {string} 'dark' or 'light'
   */
  function getTheme() {
    return document.documentElement.classList.contains('dark') ? 'dark' : 'light';
  }

  /**
   * Check if dark mode is active
   * @returns {boolean}
   */
  function isDarkMode() {
    return document.documentElement.classList.contains('dark');
  }

  /**
   * Listen for theme changes from other tabs/windows
   */
  function setupStorageListener() {
    window.addEventListener('storage', function(e) {
      if (e.key === THEME_KEY) {
        if (e.newValue === 'dark') {
          document.documentElement.classList.add('dark');
        } else {
          document.documentElement.classList.remove('dark');
        }
        
        window.dispatchEvent(new CustomEvent('themeChanged', { 
          detail: { theme: e.newValue }
        }));
      }
    });
  }

  /**
   * Setup theme toggle button listeners
   */
  function setupToggleListeners() {
    // Find all theme toggle buttons
    const toggleButtons = document.querySelectorAll('[data-theme-toggle]');
    
    console.log('[ThemeController] Found', toggleButtons.length, 'theme toggle buttons');
    
    toggleButtons.forEach(button => {
      button.addEventListener('click', function(e) {
        e.preventDefault();
        console.log('[ThemeController] Toggle button clicked');
        toggleTheme();
      });
    });
  }

  // Initialize immediately
  initTheme();
  
  // Setup storage listener and toggle buttons when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
      console.log('[ThemeController] DOMContentLoaded - Setting up listeners');
      setupStorageListener();
      setupToggleListeners();
    });
  } else {
    console.log('[ThemeController] DOM already loaded - Setting up listeners immediately');
    setupStorageListener();
    setupToggleListeners();
  }

  // Expose API globally
  window.ThemeController = {
    toggle: toggleTheme,
    set: setTheme,
    get: getTheme,
    isDark: isDarkMode,
    init: initTheme
  };

  // Also expose legacy function names for backward compatibility
  window.toggleDarkMode = toggleTheme;
})();
