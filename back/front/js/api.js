const API_BASE = 'http://localhost:8080/api';

async function apiFetch(path, options = {}) {
    try {
        const res = await fetch(API_BASE + path, {
            headers: { 'Content-Type': 'application/json', ...options.headers },
            ...options
        });
        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || `Erreur ${res.status}`);
        }
        const text = await res.text();
        return text ? JSON.parse(text) : null;
    } catch (e) {
        throw e;
    }
}

// ── Categories ──
const CategoryAPI = {
    getAll:  ()         => apiFetch('/categories'),
    create:  (data)     => apiFetch('/categories', { method: 'POST', body: JSON.stringify(data) }),
    update:  (id, data) => apiFetch(`/categories/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    toggle:  (id)       => apiFetch(`/categories/${id}/toggle`, { method: 'PUT' }),
    delete:  (id)       => apiFetch(`/categories/${id}`, { method: 'DELETE' }),
};

// ── Policies ──
const PolicyAPI = {
    getAll:   ()         => apiFetch('/policies'),
    create:   (data)     => apiFetch('/policies', { method: 'POST', body: JSON.stringify(data) }),
    update:   (id, data) => apiFetch(`/policies/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    activate: (id)       => apiFetch(`/policies/${id}/activate`, { method: 'PUT' }),
    delete:   (id)       => apiFetch(`/policies/${id}`, { method: 'DELETE' }),
};

// ── Blocked Domains ──
const DomainAPI = {
    getAll:  ()     => apiFetch('/domains'),
    create:  (data) => apiFetch('/domains', { method: 'POST', body: JSON.stringify(data) }),
    delete:  (id)   => apiFetch(`/domains/${id}`, { method: 'DELETE' }),
    import:  (formData) => fetch(API_BASE + '/domains/import', { method: 'POST', body: formData })
                            .then(r => r.text()),
};

// ── DNS Logs ──
const LogAPI = {
    getLogs: (params = {}) => {
        const q = new URLSearchParams(params).toString();
        return apiFetch('/logs' + (q ? '?' + q : ''));
    },
    getStats: () => apiFetch('/logs/stats'),
};

// ── Reports ──
const ReportAPI = {
    getAll:    ()                       => apiFetch('/reports'),
    generate:  (type, from, to)         => apiFetch(`/reports/generate?type=${type}&from=${from}&to=${to}`, { method: 'POST' }),
    delete:    (id)                     => apiFetch(`/reports/${id}`, { method: 'DELETE' }),
};

// ── Toast ──
function showToast(msg, type = 'success') {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.className = 'show ' + type;
    setTimeout(() => t.className = '', 3000);
}

// ── Modal ──
function openModal(id)  { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }