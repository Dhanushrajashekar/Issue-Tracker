requireAuth();
fillSidebarUser();

const user = getCurrentUser();
if (user?.role === 'ROLE_ADMIN') document.getElementById('adminLink').style.display = '';

const projectId = new URLSearchParams(window.location.search).get('id');
if (!projectId) window.location.href = 'projects.html';

let allIssues = [];
let projectMembers = [];

async function load() {
    try {
        const [project, issues] = await Promise.all([
            apiFetch(`/api/projects/${projectId}`),
            apiFetch(`/api/issues/project/${projectId}`)
        ]);

        allIssues = issues;
        projectMembers = project.members || [];

        // Populate header
        document.getElementById('topbarProjectName').textContent = project.name;
        document.getElementById('projectKeyBadge').textContent   = project.projectKey;
        document.getElementById('projectDesc').textContent        = project.description || '';
        document.title = `${project.name} — Issue Tracker`;

        // Members row
        document.getElementById('membersRow').innerHTML =
            '<i class="bi bi-people me-1"></i>' +
            projectMembers.map(m =>
                `<span class="watcher-chip me-1">${initials(m.name)} ${m.name}</span>`
            ).join('');

        // Populate assignee dropdown in new-issue modal
        const sel = document.getElementById('issueAssignee');
        projectMembers.forEach(m => {
            const opt = document.createElement('option');
            opt.value = m.id;
            opt.textContent = m.name;
            sel.appendChild(opt);
        });

        renderIssues(issues);
    } catch (err) {
        document.getElementById('alert-area').innerHTML =
            `<div class="alert alert-danger">${err.message}</div>`;
    }
}

function renderIssues(issues) {
    const tbody = document.getElementById('issuesTableBody');
    if (!issues.length) {
        tbody.innerHTML = `<tr><td colspan="7"><div class="empty-state">
            <i class="bi bi-bug"></i><p>No issues yet. Click <strong>New Issue</strong> to file the first one.</p>
        </div></td></tr>`;
        return;
    }
    tbody.innerHTML = issues.map(i => `
        <tr>
            <td>${typeBadge(i.type)}</td>
            <td><a href="issue-detail.html?id=${i.id}">${i.title}</a></td>
            <td>${statusBadge(i.status)}</td>
            <td>${priorityBadge(i.priority)}</td>
            <td style="font-size:.8rem; color:var(--text-muted);">
                ${i.assignee ? `<span class="watcher-chip">${initials(i.assignee.name)} ${i.assignee.name}</span>` : '<span style="color:var(--text-light);">—</span>'}
            </td>
            <td style="font-size:.75rem; color:var(--text-muted);">${timeAgo(i.createdAt)}</td>
            <td>
                <button class="btn-icon text-danger" onclick="deleteIssue(${i.id})" title="Delete">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

// Client-side filtering
function applyFilters() {
    const status   = document.getElementById('filterStatus').value;
    const priority = document.getElementById('filterPriority').value;
    const type     = document.getElementById('filterType').value;
    const search   = document.getElementById('filterSearch').value.toLowerCase();

    const filtered = allIssues.filter(i =>
        (!status   || i.status === status) &&
        (!priority || i.priority === priority) &&
        (!type     || i.type === type) &&
        (!search   || i.title.toLowerCase().includes(search))
    );
    renderIssues(filtered);
}

['filterStatus', 'filterPriority', 'filterType'].forEach(id =>
    document.getElementById(id).addEventListener('change', applyFilters)
);
document.getElementById('filterSearch').addEventListener('input', applyFilters);

// Create issue
document.getElementById('createIssueBtn').addEventListener('click', async () => {
    const title = document.getElementById('issueTitle').value.trim();
    if (!title) { showAlert('issue-modal-alert', 'Title is required.'); return; }

    const assigneeId = document.getElementById('issueAssignee').value;
    try {
        await apiFetch('/api/issues', {
            method: 'POST',
            body: JSON.stringify({
                title,
                description: document.getElementById('issueDescription').value.trim(),
                projectId: parseInt(projectId),
                type: document.getElementById('issueType').value,
                priority: document.getElementById('issuePriority').value,
                assigneeId: assigneeId ? parseInt(assigneeId) : null
            })
        });
        bootstrap.Modal.getInstance(document.getElementById('newIssueModal')).hide();
        load();
    } catch (err) {
        showAlert('issue-modal-alert', err.message);
    }
});

// Add member
document.getElementById('addMemberBtn').addEventListener('click', async () => {
    const email = document.getElementById('memberEmail').value.trim();
    if (!email) { showAlert('member-modal-alert', 'Email is required.'); return; }
    try {
        await apiFetch(`/api/projects/${projectId}/members`, {
            method: 'POST',
            body: JSON.stringify({ email })
        });
        bootstrap.Modal.getInstance(document.getElementById('addMemberModal')).hide();
        load();
    } catch (err) {
        showAlert('member-modal-alert', err.message);
    }
});

async function deleteIssue(id) {
    if (!confirm('Delete this issue? This cannot be undone.')) return;
    try {
        await apiFetch(`/api/issues/${id}`, { method: 'DELETE' });
        load();
    } catch (err) {
        showAlert('alert-area', err.message);
    }
}

load();
