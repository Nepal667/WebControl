async function loadStats() {
    try {
        const stats = await LogAPI.getStats();

        document.getElementById('stat-total').textContent   = stats.totalRequests.toLocaleString();
        document.getElementById('stat-blocked').textContent = stats.blockedRequests.toLocaleString();
        document.getElementById('stat-allowed').textContent = stats.allowedRequests.toLocaleString();
        document.getElementById('stat-rate').textContent    = stats.blockingRate.toFixed(1) + '%';

        renderTopDomains('top-domains-body', stats.topDomains);
        renderTopDomains('top-blocked-body', stats.topBlockedDomains);

    } catch (e) {
        showToast('Erreur chargement stats : ' + e.message, 'error');
    }
}

function renderTopDomains(tbodyId, list) {
    const tbody = document.getElementById(tbodyId);
    if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;color:var(--text-muted);padding:24px;">Aucune donnée</td></tr>`;
        return;
    }
    tbody.innerHTML = list.map((item, i) => `
        <tr>
            <td><span class="badge badge-muted">${i + 1}</span></td>
            <td>${item.domain}</td>
            <td><strong>${item.count.toLocaleString()}</strong></td>
        </tr>
    `).join('');
}

async function loadRecentLogs() {
    try {
        const data = await LogAPI.getLogs({ page: 0, size: 10 });
        const logs = data.content || [];
        const tbody = document.getElementById('recent-logs-body');

        if (logs.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--text-muted);padding:24px;">Aucun log disponible</td></tr>`;
            return;
        }

        tbody.innerHTML = logs.map(log => `
            <tr>
                <td>${formatTime(log.timestamp)}</td>
                <td>${log.domain}</td>
                <td><code>${log.clientIp}</code></td>
                <td>${statusBadge(log.status)}</td>
                <td>${log.responseTime != null ? log.responseTime + ' ms' : '—'}</td>
            </tr>
        `).join('');

    } catch (e) {
        showToast('Erreur chargement logs : ' + e.message, 'error');
    }
}

function statusBadge(status) {
    return status === 'BLOCKED'
        ? `<span class="badge badge-danger">Bloqué</span>`
        : `<span class="badge badge-success">Autorisé</span>`;
}

function formatTime(ts) {
    if (!ts) return '—';
    const d = new Date(ts);
    return d.toLocaleTimeString('fr-FR') + ' ' + d.toLocaleDateString('fr-FR');
}

async function loadAll() {
    await Promise.all([loadStats(), loadRecentLogs()]);
}

// Chargement initial + auto-refresh toutes les 30s
loadAll();
setInterval(loadAll, 30000);