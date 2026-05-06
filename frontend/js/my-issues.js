requireAuth();
fillSidebarUser();

const user = getCurrentUser();
if (user?.role === 'ROLE_ADMIN') document.getElementById('adminLink').style.display = '';

let currentTab = 'assigned';

async function load() {
    document.getElementById('issuesBody').innerHTML =
        '<tr><td colspan="6" class="text-center py-4"><div class="spinner-border spinner-border-sm"></div></td></tr>';
    try {
        const endpoint = currentTab === 'assigned' ? '/api/issues/my/assigned' : '/api/issues/my/reported';
        const issues = await apiFetch(endpoint);
        render(issues);
    } catch (err) {
        document.getElementById('issuesBody').innerHTML =
            `<tr><td colspan="6" class="text-center text-danger py-4">${err.message}</td></tr>`;
    }
}

function render(issues) {
    const tbody = document.getElementById('issuesBody');
    if (!issues.length) {
        tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state">
            <i class="bi bi-check-circle"></i>
            <p>${currentTab === 'assigned' ? 'Nothing assigned to you.' : 'You haven\'t reported any issues.'}</p>
        </div></td></tr>`;
        return;
    }
    tbody.innerHTML = issues.map(i => `
        <tr>
            <td>${typeBadge(i.type)}</td>
            <td><a href="issue-detail.html?id=${i.id}">${i.title}</a></td>
            <td>
                <a href="project-detail.html?id=${i.project?.id}" style="font-size:.78rem; color:var(--text-muted); text-decoration:none;">
                    <span class="project-key" style="margin:0; font-size:.65rem;">${i.project?.projectKey || '—'}</span>
                </a>
            </td>
            <td>${statusBadge(i.status)}</td>
            <td>${priorityBadge(i.priority)}</td>
            <td style="font-size:.75rem; color:var(--text-muted);">${timeAgo(i.createdAt)}</td>
        </tr>
    `).join('');
}

window.showTab = function(tab) {
    currentTab = tab;
    const assigned = document.getElementById('tabAssigned');
    const reported = document.getElementById('tabReported');
    if (tab === 'assigned') {
        assigned.style.background = 'var(--primary)'; assigned.style.color = 'white'; assigned.style.border = 'none';
        reported.style.background = 'none'; reported.style.color = 'var(--text-muted)'; reported.style.border = '1px solid var(--border)';
    } else {
        reported.style.background = 'var(--primary)'; reported.style.color = 'white'; reported.style.border = 'none';
        assigned.style.background = 'none'; assigned.style.color = 'var(--text-muted)'; assigned.style.border = '1px solid var(--border)';
    }
    load();
};

load();
