let currentPage = 0;
const pageSize  = 50;

async function loadLogs() {
    const domain = document.getElementById('filter-domain').value.trim();
    const status = document.getElementById('filter-status').value;

    const params = { page: currentPage, size: pageSize };
    if (domain) params.domain = domain;
    if (status) params.status = status;

    try {
        const data = await LogAPI.getLogs(params);
        renderLogs(data.content || []);
        renderPagination(data.totalPages || 0, data.number || 0);
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

function renderLogs(logs) {
    const tbody = document.getElementById('logs-body');

    if (logs.length === 0) {
        tbody.innerHTML = `
            <tr><td colspan="5">
                <div class="empty-state">
                    <div class="icon">📋</div>
                    <div>Aucun log trouvé</div>
                </div>
            </td></tr>`;
        return;
    }

    tbody.innerHTML = logs.map(log => `
        <tr>
            <td style="white-space:nowrap">${formatDateTime(log.timestamp)}</td>
            <td>${log.domain}</td>
            <td><code>${log.clientIp}</code></td>
            <td>${statusBadge(log.status)}</td>
            <td>${log.responseTime != null ? log.responseTime + ' ms' : '—'}</td>
        </tr>
    `).join('');
}

function renderPagination(totalPages, current) {
    const div = document.getElementById('pagination');
    if (totalPages <= 1) { div.innerHTML = ''; return; }

    let html = `<button onclick="goPage(${current - 1})" ${current === 0 ? 'disabled' : ''}>◀</button>`;

    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || Math.abs(i - current) <= 2) {
            html += `<button class="${i === current ? 'active' : ''}" onclick="goPage(${i})">${i + 1}</button>`;
        } else if (Math.abs(i - current) === 3) {
            html += `<button disabled>…</button>`;
        }
    }

    html += `<button onclick="goPage(${current + 1})" ${current === totalPages - 1 ? 'disabled' : ''}>▶</button>`;
    div.innerHTML = html;
}

function goPage(p) {
    currentPage = p;
    loadLogs();
}

function statusBadge(status) {
    return status === 'BLOCKED'
        ? `<span class="badge badge-danger">Bloqué</span>`
        : `<span class="badge badge-success">Autorisé</span>`;
}

function formatDateTime(ts) {
    if (!ts) return '—';
    const d = new Date(ts);
    return d.toLocaleDateString('fr-FR') + ' ' + d.toLocaleTimeString('fr-FR');
}

// Filtres avec debounce
let debounceTimer;
document.getElementById('filter-domain').addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => { currentPage = 0; loadLogs(); }, 400);
});

document.getElementById('filter-status').addEventListener('change', () => {
    currentPage = 0;
    loadLogs();
});

loadLogs();