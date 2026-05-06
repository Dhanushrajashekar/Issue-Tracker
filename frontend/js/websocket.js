// Connects to the backend WebSocket and pushes real-time notifications to the browser.
// Uses SockJS (HTTP fallback) + STOMP (messaging protocol).
// Call connectWebSocket(callback) once after login — it stays open for the session.

let stompClient = null;

function connectWebSocket(onNotification) {
    const token = localStorage.getItem('token');
    if (!token || stompClient) return;

    const socket = new SockJS(`${API_BASE}/ws`);
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        connectHeaders: { Authorization: `Bearer ${token}` },
        reconnectDelay: 5000,
        onConnect: () => {
            // Subscribe to our personal notification queue
            stompClient.subscribe('/user/queue/notifications', (message) => {
                const notification = JSON.parse(message.body);
                if (onNotification) onNotification(notification);
                showNotificationToast(notification);
                updateBadge();
            });
        },
        onStompError: () => { stompClient = null; }
    });

    stompClient.activate();
}

function showNotificationToast(notification) {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const id = 'toast-' + Date.now();
    container.insertAdjacentHTML('beforeend', `
        <div id="${id}" class="toast show align-items-center text-bg-dark border-0 mb-2" role="alert">
            <div class="d-flex">
                <div class="toast-body" style="font-size:.8rem; max-width:280px;">
                    <strong style="color:#a5b4fc;">New notification</strong><br>
                    ${notification.message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `);

    setTimeout(() => { document.getElementById(id)?.remove(); }, 5000);
}

async function updateBadge() {
    try {
        const data = await apiFetch('/api/notifications');
        const badge = document.getElementById('notification-badge');
        if (!badge) return;
        if (data.unreadCount > 0) {
            badge.textContent = data.unreadCount;
            badge.classList.remove('d-none');
        } else {
            badge.classList.add('d-none');
        }
    } catch (_) {}
}
