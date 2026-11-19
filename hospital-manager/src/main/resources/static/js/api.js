function getToken() {
    return sessionStorage.getItem('jwt_token');
}

function getUser() {
    const raw = sessionStorage.getItem('user_info');
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch (e) {
        return null;
    }
}

function setAuth(data) {
    sessionStorage.setItem('jwt_token', data.token);
    sessionStorage.setItem('user_info', JSON.stringify({
        username: data.username,
        fullName: data.fullName,
        role: data.role
    }));
}

function clearAuth() {
    sessionStorage.removeItem('jwt_token');
    sessionStorage.removeItem('user_info');
    window.location.href = 'login.html';
}

function requireAuth() {
    const token = getToken();
    if (!token) {
        window.location.href = 'login.html';
        return null;
    }
    return getUser();
}

async function authFetch(url, options = {}) {
    const token = getToken();
    const headers = options.headers ? { ...options.headers } : {};

    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    if (options.body && typeof options.body === 'string' && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    options.headers = headers;

    const response = await fetch(url, options);

    if (response.status === 401) {
        clearAuth();
        throw new Error('Session expired or unauthorized. Please log in again.');
    }

    return response;
}

function renderNavbar(activePage) {
    const user = getUser();
    const navContainer = document.getElementById('navbar-container');
    if (!navContainer) return;

    if (!user) {
        navContainer.innerHTML = `
            <nav class="navbar">
                <a href="index.html" class="navbar-brand">Hospital Management System</a>
                <ul class="navbar-nav">
                    <li><a href="login.html" class="${activePage === 'login' ? 'active' : ''}">Login</a></li>
                    <li><a href="register.html" class="${activePage === 'register' ? 'active' : ''}">Register</a></li>
                </ul>
            </nav>
        `;
        return;
    }

    let links = `<li><a href="dashboard.html" class="${activePage === 'dashboard' ? 'active' : ''}">Dashboard</a></li>`;

    if (user.role === 'ADMIN') {
        links += `
            <li><a href="patients.html" class="${activePage === 'patients' ? 'active' : ''}">Patients</a></li>
            <li><a href="doctors.html" class="${activePage === 'doctors' ? 'active' : ''}">Doctors</a></li>
            <li><a href="appointments.html" class="${activePage === 'appointments' ? 'active' : ''}">Appointments</a></li>
            <li><a href="records.html" class="${activePage === 'records' ? 'active' : ''}">Medical Records</a></li>
        `;
    } else if (user.role === 'DOCTOR') {
        links += `
            <li><a href="patients.html" class="${activePage === 'patients' ? 'active' : ''}">Patients</a></li>
            <li><a href="appointments.html" class="${activePage === 'appointments' ? 'active' : ''}">My Appointments</a></li>
            <li><a href="records.html" class="${activePage === 'records' ? 'active' : ''}">Medical Records</a></li>
        `;
    } else if (user.role === 'PATIENT') {
        links += `
            <li><a href="doctors.html" class="${activePage === 'doctors' ? 'active' : ''}">View Doctors</a></li>
            <li><a href="appointments.html" class="${activePage === 'appointments' ? 'active' : ''}">My Appointments</a></li>
            <li><a href="records.html" class="${activePage === 'records' ? 'active' : ''}">My Medical Records</a></li>
        `;
    }

    navContainer.innerHTML = `
        <nav class="navbar">
            <a href="dashboard.html" class="navbar-brand">Hospital Management System</a>
            <ul class="navbar-nav">
                ${links}
                <li><span class="user-badge">${user.fullName || user.username} (${user.role})</span></li>
                <li><button class="btn-logout" onclick="clearAuth()">Logout</button></li>
            </ul>
        </nav>
    `;
}

function showAlert(elementId, message, type = 'danger') {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.className = `alert alert-${type}`;
    el.innerText = message;
    el.style.display = 'block';
}

function hideAlert(elementId) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.style.display = 'none';
}
