// Global config — loaded on every page
// Change API_BASE to '' when using Nginx proxy in production
const API_BASE = 'http://localhost:8081';

async function apiFetch(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });

    if (response.status === 401) {
        localStorage.clear();
        window.location.href = 'index.html';
        return;
    }

    // File downloads return non-JSON — caller handles those directly
    const contentType = response.headers.get('Content-Type') || '';
    if (!contentType.includes('application/json')) return response;

    const data = await response.json();
    if (!response.ok) throw new Error(data.message || `Request failed (${response.status})`);
    return data;
}

function requireAuth() {
    if (!localStorage.getItem('token')) window.location.href = 'index.html';
}

function getCurrentUser() {
    const u = localStorage.getItem('user');
    return u ? JSON.parse(u) : null;
}

function logout() {
    localStorage.clear();
    window.location.href = 'index.html';
}

function showAlert(containerId, message, type = 'danger') {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = `<div class="alert alert-${type} alert-dismissible fade show" role="alert">
        ${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>`;
    setTimeout(() => { const a = el.querySelector('.alert'); if (a) a.remove(); }, 5000);
}

function formatDate(str) {
    if (!str) return '—';
    return new Date(str).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function timeAgo(str) {
    const diff = Date.now() - new Date(str).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1)  return 'just now';
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24)  return `${hrs}h ago`;
    const days = Math.floor(hrs / 24);
    return `${days}d ago`;
}

function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
}

// Returns initials from a full name e.g. "Alice Smith" → "AS"
function initials(name) {
    if (!name) return '?';
    return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
}

// Render a status badge
function statusBadge(status) {
    const labels = { OPEN: 'Open', IN_PROGRESS: 'In Progress', IN_REVIEW: 'In Review', RESOLVED: 'Resolved', CLOSED: 'Closed' };
    return `<span class="badge-status ${status.toLowerCase().replace('_', '_')}">${labels[status] || status}</span>`;
}

// Render a priority badge
function priorityBadge(priority) {
    const icons = { LOW: '↓', MEDIUM: '→', HIGH: '↑', CRITICAL: '⚡' };
    return `<span class="badge-priority ${priority.toLowerCase()}">${icons[priority] || ''} ${priority}</span>`;
}

// Render a type badge
function typeBadge(type) {
    const icons = { BUG: '🐛', FEATURE: '✨', TASK: '✓', IMPROVEMENT: '⬆' };
    return `<span class="badge-type ${type.toLowerCase()}">${icons[type] || ''} ${type}</span>`;
}

// Fill in the sidebar user info on every app page
function fillSidebarUser() {
    const user = getCurrentUser();
    if (!user) return;
    const av = document.getElementById('sidebarAvatar');
    const nm = document.getElementById('sidebarName');
    const rl = document.getElementById('sidebarRole');
    if (av) av.textContent = initials(user.name);
    if (nm) nm.textContent = user.name;
    if (rl) {
        const roleMap = { ROLE_ADMIN: 'Admin', ROLE_DEVELOPER: 'Developer', ROLE_REPORTER: 'Reporter' };
        rl.textContent = roleMap[user.role] || user.role;
    }
}
