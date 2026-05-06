requireAuth();
fillSidebarUser();

const user = getCurrentUser();
if (user?.role === 'ROLE_ADMIN') document.getElementById('adminLink').style.display = '';

async function load() {
    try {
        const projects = await apiFetch('/api/projects');
        renderGrid(projects);
    } catch (err) {
        document.getElementById('projectsGrid').innerHTML =
            `<div class="col-12 text-center text-danger">${err.message}</div>`;
    }
}

function renderGrid(projects) {
    const grid = document.getElementById('projectsGrid');
    if (!projects.length) {
        grid.innerHTML = `<div class="col-12">
            <div class="empty-state" style="padding:4rem;">
                <i class="bi bi-folder-plus"></i>
                <p>No projects yet. Click <strong>New Project</strong> to create your first one.</p>
            </div></div>`;
        return;
    }
    grid.innerHTML = projects.map(p => `
        <div class="col-sm-6 col-xl-4">
            <a href="project-detail.html?id=${p.id}" class="project-card">
                <div class="project-key">${p.projectKey}</div>
                <div class="project-name">${p.name}</div>
                <div class="project-desc">${p.description || 'No description provided.'}</div>
                <div class="mt-2" style="font-size:.75rem; color:var(--text-muted);">
                    <i class="bi bi-people"></i> ${p.members?.length || 0} member${p.members?.length !== 1 ? 's' : ''}
                    &nbsp;·&nbsp;
                    <i class="bi bi-person"></i> ${p.owner?.name || '—'}
                </div>
            </a>
        </div>
    `).join('');
}

// Create project
document.getElementById('createProjectBtn').addEventListener('click', async () => {
    const name = document.getElementById('projectName').value.trim();
    const key  = document.getElementById('projectKey').value.trim().toUpperCase();
    const desc = document.getElementById('projectDesc').value.trim();

    if (!name || !key) { showAlert('modal-alert', 'Name and Key are required.'); return; }
    if (!/^[A-Z]{2,10}$/.test(key)) { showAlert('modal-alert', 'Key must be 2–10 uppercase letters.'); return; }

    try {
        const project = await apiFetch('/api/projects', {
            method: 'POST',
            body: JSON.stringify({ name, projectKey: key, description: desc })
        });
        bootstrap.Modal.getInstance(document.getElementById('newProjectModal')).hide();
        window.location.href = `project-detail.html?id=${project.id}`;
    } catch (err) {
        showAlert('modal-alert', err.message);
    }
});

// Auto-uppercase the key field as user types
document.getElementById('projectKey').addEventListener('input', function () {
    this.value = this.value.toUpperCase().replace(/[^A-Z]/g, '');
});

load();
