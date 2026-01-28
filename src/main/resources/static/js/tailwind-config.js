/**
 * Centralized Tailwind CSS Configuration - ABClinic Design System
 * 
 * This file provides a unified design system configuration for the entire application.
 * Include this file in templates BEFORE the Tailwind CDN script.
 * 
 * Usage in HTML:
 * <script src="/static/js/tailwind-config.js"></script>
 * <script src="https://cdn.tailwindcss.com"></script>
 * 
 * @version 2.0.0 - ABClinic Theme
 * @date January 28, 2026
 */

tailwind.config = {
    darkMode: 'class',
    theme: {
        extend: {
            colors: {
                // ABClinic Primary Color Palette (Blue-based)
                // Main brand color for buttons, links, and primary actions
                primary: {
                    DEFAULT: '#0d7cf2',
                    50: '#eff6ff',
                    100: '#dbeafe',
                    200: '#bfdbfe',
                    300: '#93c5fd',
                    400: '#60a5fa',
                    500: '#0d7cf2',  // ABClinic main brand color
                    600: '#0a62bf',  // primary-dark
                    700: '#1d4ed8',
                    800: '#1e40af',
                    900: '#1e3a8a',
                },
                // Secondary Color Palette (Emerald)
                // Used for success states and secondary actions
                secondary: {
                    50: '#ecfdf5',
                    100: '#d1fae5',
                    200: '#a7f3d0',
                    300: '#6ee7b7',
                    400: '#34d399',
                    500: '#10b981',
                    600: '#059669',
                    700: '#047857',
                    800: '#065f46',
                    900: '#064e3b',
                },
                // Surface/Background Colors
                background: {
                    light: '#f5f7f8',
                    dark: '#101922',
                },
                card: {
                    light: '#ffffff',
                    dark: '#1e293b',
                },
                // Surface/Neutral Color Palette (Slate)
                surface: {
                    50: '#f8fafc',
                    100: '#f1f5f9',
                    200: '#e2e8f0',
                    300: '#cbd5e1',
                    400: '#94a3b8',
                    500: '#64748b',
                    600: '#475569',
                    700: '#334155',
                    800: '#1e293b',
                    900: '#0f172a',
                },
                // Semantic Colors
                success: {
                    50: '#f0fdf4',
                    100: '#dcfce7',
                    500: '#22c55e',
                    600: '#16a34a',
                    700: '#15803d',
                },
                warning: {
                    50: '#fffbeb',
                    100: '#fef3c7',
                    500: '#f59e0b',
                    600: '#d97706',
                    700: '#b45309',
                },
                error: {
                    50: '#fef2f2',
                    100: '#fee2e2',
                    500: '#ef4444',
                    600: '#dc2626',
                    700: '#b91c1c',
                },
                info: {
                    50: '#eff6ff',
                    100: '#dbeafe',
                    500: '#3b82f6',
                    600: '#2563eb',
                    700: '#1d4ed8',
                },
            },
            fontFamily: {
                display: ['Be Vietnam Pro', 'system-ui', '-apple-system', 'sans-serif'],
                sans: ['Be Vietnam Pro', 'system-ui', '-apple-system', 'sans-serif'],
                body: ['Roboto', 'system-ui', '-apple-system', 'sans-serif'],
                heading: ['Be Vietnam Pro', 'system-ui', '-apple-system', 'sans-serif'],
            },
            fontSize: {
                'xs': ['0.75rem', { lineHeight: '1rem' }],
                'sm': ['0.875rem', { lineHeight: '1.25rem' }],
                'base': ['1rem', { lineHeight: '1.5rem' }],
                'lg': ['1.125rem', { lineHeight: '1.75rem' }],
                'xl': ['1.25rem', { lineHeight: '1.75rem' }],
                '2xl': ['1.5rem', { lineHeight: '2rem' }],
                '3xl': ['1.875rem', { lineHeight: '2.25rem' }],
                '4xl': ['2.25rem', { lineHeight: '2.5rem' }],
                '5xl': ['3rem', { lineHeight: '1' }],
            },
            spacing: {
                '18': '4.5rem',
                '88': '22rem',
                '112': '28rem',
                '128': '32rem',
            },
            borderRadius: {
                DEFAULT: '0.25rem',
                'lg': '0.5rem',
                'xl': '0.75rem',
                '2xl': '1rem',
                '3xl': '1.5rem',
                'full': '9999px',
            },
            boxShadow: {
                'soft': '0 2px 8px rgba(0, 0, 0, 0.05)',
                'hover': '0 4px 12px rgba(0, 0, 0, 0.1)',
                'card': '0 1px 3px rgba(0, 0, 0, 0.05)',
                'primary': '0 4px 14px rgba(13, 124, 242, 0.25)',
                'secondary': '0 4px 14px rgba(16, 185, 129, 0.25)',
            },
            transitionDuration: {
                '250': '250ms',
                '350': '350ms',
            },
            animation: {
                'fade-in': 'fadeIn 0.3s ease-in-out',
                'slide-up': 'slideUp 0.3s ease-out',
                'slide-down': 'slideDown 0.3s ease-out',
                'slide-left': 'slideLeft 0.3s ease-out',
                'slide-right': 'slideRight 0.3s ease-out',
                'scale-in': 'scaleIn 0.2s ease-out',
                'pulse-ring': 'pulseRing 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite',
            },
            keyframes: {
                fadeIn: {
                    '0%': { opacity: '0' },
                    '100%': { opacity: '1' },
                },
                slideUp: {
                    '0%': { transform: 'translateY(10px)', opacity: '0' },
                    '100%': { transform: 'translateY(0)', opacity: '1' },
                },
                slideDown: {
                    '0%': { transform: 'translateY(-10px)', opacity: '0' },
                    '100%': { transform: 'translateY(0)', opacity: '1' },
                },
                slideLeft: {
                    '0%': { transform: 'translateX(10px)', opacity: '0' },
                    '100%': { transform: 'translateX(0)', opacity: '1' },
                },
                slideRight: {
                    '0%': { transform: 'translateX(-10px)', opacity: '0' },
                    '100%': { transform: 'translateX(0)', opacity: '1' },
                },
                scaleIn: {
                    '0%': { transform: 'scale(0.95)', opacity: '0' },
                    '100%': { transform: 'scale(1)', opacity: '1' },
                },
                pulseRing: {
                    '0%': { transform: 'scale(0.95)', opacity: '1' },
                    '75%, 100%': { transform: 'scale(1.3)', opacity: '0' },
                },
            },
        },
    },
    plugins: [],
};

/**
 * ABClinic Design System Notes:
 * 
 * 1. Color Palette:
 *    - Primary: ABClinic Blue (#0d7cf2) - brand color
 *    - Primary Dark: #0a62bf - for hover states
 *    - Background Light: #f5f7f8 - main page background
 *    - Background Dark: #101922 - dark mode background
 * 
 * 2. Typography:
 *    - Headings & Body: Be Vietnam Pro (Vietnamese optimized)
 *    - Alternative: Roboto
 *    - Import in HTML: fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700
 * 
 * 3. Dark Mode:
 *    - Class-based: add 'dark' class to <html> element
 *    - Colors automatically adjust via dark: prefix
 * 
 * 4. Consistent Design:
 *    - Rounded corners: 0.5rem (lg) to 1rem (2xl)
 *    - Shadows: soft for cards, hover for interactions
 *    - Transitions: 200-350ms for smooth UX
 */
