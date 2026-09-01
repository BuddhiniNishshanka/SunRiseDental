/**
 * Sunrise Dental Clinic - Distributed REST API Client
 */

// Dynamically determine the context path (e.g. /project/api or /api)
const API_BASE = window.location.pathname.includes('/SunRiseDental')
    ? window.location.origin + '/SunRiseDental/api'
    : window.location.pathname.includes('/sunrise-dental')
    ? window.location.origin + '/sunrise-dental/api'
    : window.location.origin + '/api';

const API = {
    // Authentication
    async login(username, password) {
        return this.post('/auth/login', { username, password });
    },

    async logout() {
        return this.post('/auth/logout', {});
    },

    async getSession() {
        return this.get('/auth/session');
    },

    // Appointments
    async getAllAppointments() {
        return this.get('/appointments');
    },

    async getTodayAppointments() {
        return this.get('/appointments?today=true');
    },

    async searchAppointmentByNo(appNo) {
        return this.get(`/appointments/search?no=${encodeURIComponent(appNo)}`);
    },

    async searchAppointments(query) {
        return this.get(`/appointments/search?query=${encodeURIComponent(query)}`);
    },

    async createAppointment(appointmentData) {
        return this.post('/appointments', appointmentData);
    },

    async cancelAppointment(appointmentId) {
        return this.post(`/appointments/cancel?id=${appointmentId}`, {});
    },

    // Patients
    async getPatients(query = '') {
        return this.get(query ? `/patients?query=${encodeURIComponent(query)}` : '/patients');
    },

    async createPatient(patientData) {
        return this.post('/patients', patientData);
    },

    // Dentists & Treatments
    async getDentists() {
        return this.get('/dentists');
    },

    async getTreatments() {
        return this.get('/treatments');
    },

    // Billing & Invoices
    async previewBill(appointmentId, discountRate = 0, taxRate = 0) {
        return this.get(`/bills/preview?appointmentId=${appointmentId}&discountRate=${discountRate}&taxRate=${taxRate}`);
    },

    async processBill(billingData) {
        return this.post('/bills/process', billingData);
    },

    async getBillByAppointmentId(appointmentId) {
        return this.get(`/bills/search?appointmentId=${appointmentId}`);
    },

    async getBillByNo(billNo) {
        return this.get(`/bills/search?billNo=${encodeURIComponent(billNo)}`);
    },

    async getAllBills() {
        return this.get('/bills');
    },

    // Reports & Analytics
    async getDashboardMetrics() {
        return this.get('/reports/dashboard');
    },

    async getDoctorWorkload() {
        return this.get('/reports/doctor-workload');
    },

    async getRevenueReport(startDate, endDate) {
        return this.get(`/reports/revenue?start=${startDate}&end=${endDate}`);
    },

    async getNotificationLogs() {
        return this.get('/reports/notifications');
    },

    // Generic HTTP Handlers
    async get(endpoint) {
        try {
            const res = await fetch(`${API_BASE}${endpoint}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });
            return await res.json();
        } catch (err) {
            console.error(`API GET error on ${endpoint}:`, err);
            return { success: false, error: 'Network error or service unavailable.' };
        }
    },

    async post(endpoint, data) {
        try {
            const res = await fetch(`${API_BASE}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(data)
            });
            return await res.json();
        } catch (err) {
            console.error(`API POST error on ${endpoint}:`, err);
            return { success: false, error: 'Network error or service unavailable.' };
        }
    }
};

// UI Notification Toast
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
}

// Session Guard
function checkAuthAndInitHeader() {
    const userJson = sessionStorage.getItem('currentUser');
    if (!userJson && !window.location.pathname.endsWith('index.html') && !window.location.pathname.endsWith('/')) {
        // Fallback for direct opening or check session via API
        API.getSession().then(res => {
            if (res && res.success) {
                sessionStorage.setItem('currentUser', JSON.stringify(res.data));
                updateUserUI(res.data);
            } else {
                // If not logged in and not on login page, redirect
                window.location.href = 'index.html';
            }
        });
    } else if (userJson) {
        try {
            const user = JSON.parse(userJson);
            updateUserUI(user);
        } catch (e) {}
    }
}

function updateUserUI(user) {
    const avatarEl = document.getElementById('userAvatar');
    const nameEl = document.getElementById('userFullName');
    const roleEl = document.getElementById('userRole');

    if (nameEl) nameEl.textContent = user.fullName || user.username;
    if (roleEl) roleEl.textContent = user.role;
    if (avatarEl && user.fullName) {
        avatarEl.textContent = user.fullName.substring(0, 2).toUpperCase();
    }
}

async function handleLogout() {
    await API.logout();
    sessionStorage.removeItem('currentUser');
    window.location.href = 'index.html';
}

document.addEventListener('DOMContentLoaded', checkAuthAndInitHeader);
