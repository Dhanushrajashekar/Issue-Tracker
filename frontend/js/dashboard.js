requireAuth();
fillSidebarUser();

const user = getCurrentUser();
if (user?.role === 'ROLE_ADMIN') document.getElementById('adminLink').style.display = '';

async function load() {
    try {
        // Load projects, assigned issues, and notifications in parallel
        const [projects, assigned, reported, notifData] = await Promise.all([
            apiFetch('/api/projects'),
            apiFetch('/api/issues/my/assigned'),
            apiFetch('/api/issues/my/reported'),
            apiFetch('/api/notifications')
        ]);

        // Stat cards
        document.getElementById('statProjects').textContent = projects.length;
        document.getElementById('statAssigned').textContent = assigned.length;

        const open     = [...assigned, ...reported].filter(i => i.status === 'OPEN' || i.status === 'IN_PROGRESS').length;
        const resolved = [...assigned, ...reported].filter(i => i.status === 'RESOLVED' || i.status === 'CLOSED').length;
        document.getElementById('statOpen').textContent     = open;
        document.getElementById('statResolved').textContent = resolved;

        // Notification badge
        const badge = document.getElementById('notification-badge');
        if (notifData.unreadCount > 0) {
            badge.textContent = notifData.unreadCount;
            badge.classList.remove('d-none');
        }

        renderProjects(projects);
        renderAssigned(assigned);

    } catch (err) {
        console.error(err);
    }
}

function renderProjects(projects) {
    const el = document.getElementById('projectsList');
    if (!projects.length) {
        el.innerHTML = `<div class="empty-state"><i class="bi bi-folder"></i><p>No projects yet. <a href="projects.html">Create one</a></p></div>`;
        return;
    }
    el.innerHTML = projects.map(p => `
        <a href="project-detail.html?id=${p.id}" class="d-flex align-items-center gap-3 px-4 py-3 text-decoration-none"
           style="border-bottom:1px solid var(--border); transition:background .1s;"
           onmouseover="this.style.background='#f8fafc'" onmouseout="this.style.background=''">
            <span class="project-key" style="margin:0;">${p.projectKey}</span>
            <div style="flex:1; min-width:0;">
                <div style="font-weight:600; font-size:.875rem; color:var(--text);">${p.name}</div>
                <div style="font-size:.75rem; color:var(--text-muted); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                    ${p.description || 'No description'} · ${p.members?.length || 0} member${p.members?.length !== 1 ? 's' : ''}
                </div>
            </div>
            <i class="bi bi-chevron-right" style="color:var(--text-muted); font-size:.75rem;"></i>
        </a>
    `).join('');
}

function renderAssigned(issues) {
    const el = document.getElementById('assignedList');
    if (!issues.length) {
        el.innerHTML = `<div class="empty-state"><i class="bi bi-check-circle"></i><p>Nothing assigned to you</p></div>`;
        return;
    }
    el.innerHTML = issues.slice(0, 8).map(i => `
        <a href="issue-detail.html?id=${i.id}" class="d-flex align-items-center gap-2 px-4 py-3 text-decoration-none"
           style="border-bottom:1px solid var(--border);"
           onmouseover="this.style.background='#f8fafc'" onmouseout="this.style.background=''">
            ${typeBadge(i.type)}
            <span style="flex:1; font-size:.8rem; font-weight:500; color:var(--text); overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${i.title}</span>
            ${statusBadge(i.status)}
        </a>
    `).join('');
}

connectWebSocket(() => updateBadge());
load();
