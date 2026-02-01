/**
 * Reports Detail Charts Module
 * Handles Chart.js initialization for report detail page
 */

/**
 * Initialize all charts for report detail page
 */
export function init() {
    initDailyVisitsChart();
    initDepartmentChart();
}

/**
 * Initialize daily visits chart
 */
function initDailyVisitsChart() {
    const ctx = document.getElementById('dailyVisitsChart');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: Array.from({length: 31}, (_, i) => i + 1),
            datasets: [{
                label: 'Lượt khám',
                data: [145, 156, 0, 167, 178, 189, 156, 134, 145, 0, 167, 178, 189, 190, 145, 156, 167, 0, 178, 189, 201, 156, 145, 167, 0, 178, 189, 190, 156, 167, 178],
                borderColor: '#0891B2',
                backgroundColor: 'rgba(8, 145, 178, 0.1)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
}

/**
 * Initialize department distribution chart
 */
function initDepartmentChart() {
    const ctx = document.getElementById('departmentChart');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Nội khoa', 'Ngoại khoa', 'Sản phụ khoa', 'Nhi khoa', 'Da liễu', 'Mắt'],
            datasets: [{
                data: [1234, 876, 654, 567, 456, 345],
                backgroundColor: ['#0891B2', '#059669', '#F59E0B', '#8B5CF6', '#EC4899', '#06B6D4'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'right' }
            }
        }
    });
}
