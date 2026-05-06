requireAuth();
fillSidebarUser();

const user = getCurrentUser();
if (user?.role === 'ROLE_ADMIN') document.getElementById('adminLink').style.display = '';

const issueId = new URLSearchParams(window.location.search).get('id');
if (!issueId) window.location.href = 'projects.html';

let issue = null;

async function load() {
    try {
        const [issueData, comments, attachments] = await Promise.all([
            apiFetch(`/api/issues/${issueId}`),
            apiFetch(`/api/issues/${issueId}/comments`),
            apiFetch(`/api/issues/${issueId}/attachments`)
        ]);

        issue = issueData;
        renderIssue(issue);
        renderComments(comments);
        renderAttachments(attachments);

        // Populate the assignee dropdown with project members
        const project = await apiFetch(`/api/projects/${issue.project.id}`);
        const assigneeSel = document.getElementById('assigneeSelect');
        project.members.forEach(m => {
            const opt = document.createElement('option');
            opt.value = m.id;
            opt.textContent = m.name;
            if (issue.assignee?.id === m.id) opt.selected = true;
            assigneeSel.appendChild(opt);
        });

    } catch (err) {
        document.getElementById('loadingState').innerHTML =
            `<div class="alert alert-danger">${err.message}</div>`;
    }
}

function renderIssue(i) {
    document.title = `${i.title} — Issue Tracker`;
    document.getElementById('loadingState').style.display = 'none';
    document.getElementById('detailLayout').style.display = '';

    // Breadcrumb
    document.getElementById('topbarBreadcrumb').innerHTML =
        `<a href="projects.html" style="color:var(--text-muted); text-decoration:none;">Projects</a> /
         <a href="project-detail.html?id=${i.project.id}" style="color:var(--text-muted); text-decoration:none;">${i.project.name}</a> /
         <span style="font-weight:600;">${i.title}</span>`;

    document.getElementById('issueBadgeType').innerHTML    = typeBadge(i.type);
    document.getElementById('issueBadgePriority').innerHTML = priorityBadge(i.priority);
    document.getElementById('issueTitle').textContent      = i.title;
    document.getElementById('issueMeta').innerHTML =
        `Filed by <strong>${i.reporter?.name}</strong> · ${timeAgo(i.createdAt)} · ${i.watchers?.length || 0} watcher${i.watchers?.length !== 1 ? 's' : ''}`;
    document.getElementById('issueDescription').textContent = i.description || 'No description provided.';

    // Meta sidebar
    document.getElementById('statusSelect').value   = i.status;
    document.getElementById('prioritySelect').value = i.priority;
    document.getElementById('reporterName').textContent = i.reporter?.name || '—';
    document.getElementById('projectLink').textContent  = i.project?.name || '—';
    document.getElementById('projectLink').href = `project-detail.html?id=${i.project?.id}`;
    document.getElementById('createdAt').textContent = formatDate(i.createdAt);
    document.getElementById('updatedAt').textContent = formatDate(i.updatedAt);

    // Watch button
    const isWatching = i.watchers?.some(w => w.id === user?.id);
    document.getElementById('watchBtnText').textContent = isWatching ? 'Watching' : 'Watch';
    document.getElementById('watchBtn').style.background = isWatching ? 'var(--primary-light)' : '';
    document.getElementById('watchBtn').style.color      = isWatching ? 'var(--primary)' : '';

    // Watchers list
    document.getElementById('watcherCount').textContent = `(${i.watchers?.length || 0})`;
    document.getElementById('watchersList').innerHTML = (i.watchers || []).map(w =>
        `<span class="watcher-chip"><div class="user-avatar" style="width:20px;height:20px;font-size:.6rem;">${initials(w.name)}</div>${w.name}</span>`
    ).join('') || '<span style="font-size:.8rem; color:var(--text-muted);">No watchers yet</span>';
}

function renderComments(comments) {
    const el = document.getElementById('commentsList');
    document.getElementById('commentCount').textContent = `(${comments.length})`;
    if (!comments.length) {
        el.innerHTML = `<div class="empty-state" style="padding:1.5rem;"><i class="bi bi-chat"></i><p>No comments yet. Be the first to comment.</p></div>`;
        return;
    }
    el.innerHTML = comments.map(c => `
        <div class="comment">
            <div class="comment-avatar">${initials(c.author?.name)}</div>
            <div class="comment-body">
                <div class="comment-header">
                    <span class="comment-author">${c.author?.name}</span>
                    <span class="comment-time">${timeAgo(c.createdAt)}</span>
                    ${c.author?.id === user?.id ?
                        `<button class="btn-icon text-danger ms-auto" onclick="deleteComment(${c.id})" title="Delete"><i class="bi bi-trash" style="font-size:.75rem;"></i></button>`
                        : ''}
                </div>
                <div class="comment-text">${escapeHtml(c.content)}</div>
            </div>
        </div>
    `).join('');
}

function renderAttachments(attachments) {
    const el = document.getElementById('attachmentsList');
    document.getElementById('attachCount').textContent = `(${attachments.length})`;
    if (!attachments.length) {
        el.innerHTML = `<div class="empty-state"><i class="bi bi-paperclip"></i><p>No attachments yet</p></div>`;
        return;
    }
    el.innerHTML = `<div style="padding:.25rem 1rem;">` + attachments.map(a => `
        <div class="attachment-row">
            <i class="bi bi-file-earmark attachment-icon"></i>
            <div style="flex:1;">
                <div style="font-weight:500;">${escapeHtml(a.originalName)}</div>
                <div style="color:var(--text-muted); font-size:.7rem;">${formatFileSize(a.fileSize)} · uploaded by ${a.uploader?.name} · ${timeAgo(a.createdAt)}</div>
            </div>
            <a href="${API_BASE}/api/files/${a.storedName}" class="btn btn-sm btn-outline-secondary" target="_blank">
                <i class="bi bi-download"></i>
            </a>
            ${a.uploader?.id === user?.id || user?.role === 'ROLE_ADMIN' ?
                `<button class="btn-icon text-danger" onclick="deleteAttachment(${a.id})"><i class="bi bi-trash"></i></button>` : ''}
        </div>
    `).join('') + '</div>';
}

// Post comment
document.getElementById('submitComment').addEventListener('click', async () => {
    const content = document.getElementById('commentInput').value.trim();
    if (!content) return;
    try {
        await apiFetch(`/api/issues/${issueId}/comments`, {
            method: 'POST',
            body: JSON.stringify({ content })
        });
        document.getElementById('commentInput').value = '';
        load();
    } catch (err) {
        showAlert('alert-area', err.message);
    }
});

async function deleteComment(id) {
    if (!confirm('Delete this comment?')) return;
    try {
        await apiFetch(`/api/comments/${id}`, { method: 'DELETE' });
        load();
    } catch (err) {
        showAlert('alert-area', err.message);
    }
}

// Save issue field changes (status, priority, assignee)
document.getElementById('saveChangesBtn').addEventListener('click', async () => {
    const assigneeVal = document.getElementById('assigneeSelect').value;
    try {
        await apiFetch(`/api/issues/${issueId}`, {
            method: 'PUT',
            body: JSON.stringify({
                status:     document.getElementById('statusSelect').value,
                priority:   document.getElementById('prioritySelect').value,
                assigneeId: assigneeVal ? parseInt(assigneeVal) : -1
            })
        });
        load();
    } catch (err) {
        showAlert('alert-area', err.message);
    }
});

// Watch / unwatch
document.getElementById('watchBtn').addEventListener('click', async () => {
    const isWatching = issue?.watchers?.some(w => w.id === user?.id);
    try {
        await apiFetch(`/api/issues/${issueId}/watch`, { method: isWatching ? 'DELETE' : 'POST' });
        load();
    } catch (err) {
        showAlert('alert-area', err.message);
    }
});

// Delete issue
document.getElementById('deleteIssueBtn').addEventListener('click', async () => {
    if (!confirm('Delete this issue permanently?')) return;
    try {
        await apiFetch(`/api/issues/${issueId}`, { method: 'DELETE' });
        window.location.href = `project-detail.html?id=${issue?.project?.id}`;
    } catch (err) {
        showAlert('alert-area', err.message);
    }
});

// File upload
document.getElementById('fileInput').addEventListener('change', async (e) => {
    const files = e.target.files;
    if (!files.length) return;
    const formData = new FormData();
    for (const file of files) formData.append('file', file);

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE}/api/issues/${issueId}/attachments`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${token}` },
            body: formData
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message);
        }
        e.target.value = '';
        load();
    } catch (err) {
        showAlert('alert-area', err.message);
    }
});

async function deleteAttachment(id) {
    if (!confirm('Delete this attachment?')) return;
    try {
        await apiFetch(`/api/attachments/${id}`, { method: 'DELETE' });
        load();
    } catch (err) {
        showAlert('alert-area', err.message);
    }
}

function escapeHtml(str) {
    return str?.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;') || '';
}

connectWebSocket();
load();
