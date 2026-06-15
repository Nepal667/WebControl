function setPeriod(days) {
    const now   = new Date();
    const from  = new Date();
    from.setDate(now.getDate() - (days - 1));
    from.setHours(0, 0, 0, 0);

    document.getElementById('report-from').value = toLocalISO(from);
    document.getElementById('report-to').value   = toLocalISO(now);
}

function toLocalISO(date) {
    const pad = n => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

async function generateReport() {
    const type = document.getElementById('report-type').value;
    const from = document.getElementById('report-from').value;
    const to   = document.getElementById('report-to').value;

    if (!from || !to) { showToast('Sélectionne une période', 'error'); return; }

    const fromISO = new Date(from).toISOString();
    const toISO   = new Date(to).toISOString();

    try {
        const report = await ReportAPI.generate(type, fromISO, toISO);
        showToast('Rapport généré');
        showPreview(report, type, from, to);
        loadHistory();
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

function showPreview(report, type, from, to) {
    const card    = document.getElementById('report-preview');
    const content = document.getElementById('preview-content');
    card.style.display = 'block';

    const typeLabels = {
        GENERAL:    'Activité générale',
        BLOCKED:    'Domaines bloqués',
        CLIENTS:    'Clients les plus actifs',
        CATEGORIES: 'Par catégorie'
    };

    content.innerHTML = `
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:16px;">
            <div class="stat-card">
                <div class="stat-label">Type</div>
                <div style="font-size:16px;font-weight:600">${typeLabels[type] || type}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Du</div>
                <div style="font-size:15px;font-weight:600">${new Date(from).toLocaleDateString('fr-FR')}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Au</div>
                <div style="font-size:15px;font-weight:600">${new Date(to).toLocaleDateString('fr-FR')}</div>
            </div>
        </div>
        <div style="padding:16px;background:var(--bg);border-radius:8px;font-size:13px;color:var(--text-muted);text-align:center;">
            Rapport enregistré avec l'ID : <code>${report.id}</code>
        </div>
    `;
}

async function loadHistory() {
    try {
        const reports = await ReportAPI.getAll();
        const tbody   = document.getElementById('reports-body');

        if (reports.length === 0) {
            tbody.innerHTML = `
                <tr><td colspan="4">
                    <div class="empty-state">
                        <div class="icon">📈</div>
                        <div>Aucun rapport généré</div>
                    </div>
                </td></tr>`;
            return;
        }

        const typeLabels = {
            GENERAL:    'Activité générale',
            BLOCKED:    'Domaines bloqués',
            CLIENTS:    'Clients les plus actifs',
            CATEGORIES: 'Par catégorie'
        };

        tbody.innerHTML = reports.map(r => `
            <tr>
                <td>${new Date(r.generatedAt).toLocaleString('fr-FR')}</td>
                <td><span class="badge badge-primary">${typeLabels[r.type] || r.type}</span></td>
                <td style="font-size:13px;color:var(--text-muted)">
                    ${r.periodStart ? new Date(r.periodStart).toLocaleDateString('fr-FR') : '—'}
                    →
                    ${r.periodEnd ? new Date(r.periodEnd).toLocaleDateString('fr-FR') : '—'}
                </td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="deleteReport('${r.id}')">🗑️ Supprimer</button>
                </td>
            </tr>
        `).join('');

    } catch (e) {
        showToast('Erreur historique : ' + e.message, 'error');
    }
}

async function deleteReport(id) {
    try {
        await ReportAPI.delete(id);
        showToast('Rapport supprimé');
        loadHistory();
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

// Période par défaut : aujourd'hui
setPeriod(1);
loadHistory();