/**
 * Dashboard Charts Module
 * Handles Chart.js initialization for patient trends and treatment types
 */

/**
 * Initialize patient trend line chart
 */
function initPatientTrendChart() {
    const canvas = document.getElementById('patientTrendChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12', 'Tháng 1'],
            datasets: [{
                label: 'Bệnh nhân mới',
                data: [165, 159, 180, 181, 156, 195],
                borderColor: '#0891B2',
                backgroundColor: 'rgba(8, 145, 178, 0.1)',
                fill: true,
                tension: 0.4
            }, {
                label: 'Tái khám',
                data: [128, 148, 140, 159, 146, 160],
                borderColor: '#059669',
                backgroundColor: 'rgba(5, 150, 105, 0.1)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: '#E2E8F0'
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });
}

/**
 * Initialize treatment type doughnut chart
 */
function initTreatmentChart() {
    const canvas = document.getElementById('treatmentChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Điều trị dài hạn', 'Tái khám định kỳ', 'Theo dõi chặt', 'Điều trị cấp tính', 'Chăm sóc dự phòng'],
            datasets: [{
                data: [38, 28, 18, 10, 6],
                backgroundColor: [
                    '#0891B2',
                    '#22C55E',
                    '#F59E0B',
                    '#EF4444',
                    '#8B5CF6'
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        font: {
                            size: 12,
                            family: "'Open Sans', sans-serif"
                        },
                        usePointStyle: true,
                        pointStyle: 'circle'
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 12,
                    titleFont: {
                        size: 14,
                        family: "'Poppins', sans-serif"
                    },
                    bodyFont: {
                        size: 13
                    },
                    callbacks: {
                        label: function(context) {
                            let label = context.label || '';
                            if (label) {
                                label += ': ';
                            }
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = Math.round((context.parsed / total) * 100);
                            label += context.parsed + ' bệnh nhân (' + percentage + '%)';
                            return label;
                        }
                    }
                }
            }
        }
    });
}

/**
 * Initialize all dashboard charts
 */
export function init() {
    initPatientTrendChart();
    initTreatmentChart();
}
