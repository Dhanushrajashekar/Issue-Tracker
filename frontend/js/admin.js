requireAuth();
fillSidebarUser();

const user = getCurrentUser();
// Redirect non-admins away
if (user?.role !== 'ROLE_ADMIN') window.location.href = 'dashboard.html';

async function load() {
    try {
        const users = await apiFetch('/api/admin/users');
        render(users);
    } catch (err) {
        document.getElementById('usersBody').innerHTML =
            `<tr><td colspan="6" class="text-center text-danger py-4">${err.message}</td></tr>`;
    }
}

function render(users) {
    const tbody = document.getElementById('usersBody');
    if (!users.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No users found.</td></tr>';
        return;
    }

    const roleMap = { ROLE_ADMIN: 'admin', ROLE_DEVELOPER: 'developer', ROLE_REPORTER: 'reporter' };
    const roleLabelMap = { ROLE_ADMIN: 'Admin', ROLE_DEVELOPER: 'Developer', ROLE_REPORTER: 'Reporter' };

    tbody.innerHTML = users.map(u => `
        <tr>
            <td style="font-weight:500;">${u.name}</td>
            <td style="font-size:.8rem; color:var(--text-muted);">${u.email}</td>
            <td><span class="badge-role ${roleMap[u.role] || 'developer'}">${roleLabelMap[u.role] || u.role}</span></td>
            <td>
                <span style="font-size:.75rem; font-weight:600; color:${u.active ? 'var(--success)' : 'var(--danger)'};">
                    ${u.active ? '● Active' : '● Inactive'}
                </span>
            </td>
            <td style="font-size:.75rem; color:var(--text-muted);">${formatDate(u.createdAt)}</td>
            <td>
                ${u.id !== user.id ? `
                    <select class="form-select form-select-sm" onchange="changeRole(${u.id}, this.value)" style="font-size:.75rem; width:auto;">
                        <option value="ROLE_REPORTER"  ${u.role === 'ROLE_REPORTER'  ? 'selected' : ''}>Reporter</option>
                        <option value="ROLE_DEVELOPER" ${u.role === 'ROLE_DEVELOPER' ? 'selected' : ''}>Developer</option>
                        <option value="ROLE_ADMIN"     ${u.role === 'ROLE_ADMIN'     ? 'selected' : ''}>Admin</option>
                    </select>
                ` : '<span style="font-size:.75rem; color:var(--text-muted);">You</span>'}
            </td>
        </tr>
    `).join('');
}

window.changeRole = async function(userId, role) {
    try {
        await apiFetch(`/api/admin/users/${userId}/role`, {
            method: 'PUT',
            body: JSON.stringify({ role })
        });
        showAlert('alert-area', 'Role updated successfully.', 'success');
        load();
    } catch (err) {
        showAlert('alert-area', err.message);
    }
};

load();
