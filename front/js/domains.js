let allDomains   = [];
let allCategories = [];

async function loadAll() {
    try {
        [allDomains, allCategories] = await Promise.all([
            DomainAPI.getAll(),
            CategoryAPI.getAll()
        ]);
        populateCategorySelects();
        renderTable(allDomains);
    } catch (e) {
        showToast('Erreur chargement : ' + e.message, 'error');
    }
}

function populateCategorySelects() {
    const opts = allCategories.map(c =>
        `<option value="${c.id}">${c.name}</option>`
    ).join('');

    document.getElementById('filter-category').innerHTML =
        `<option value="">Toutes les catégories</option>` + opts;
    document.getElementById('input-category').innerHTML =
        `<option value="">— Sans catégorie —</option>` + opts;
    document.getElementById('import-category').innerHTML =
        `<option value="">— Sans catégorie —</option>` + opts;
}

function renderTable(domains) {
    const tbody = document.getElementById('domains-body');
    document.getElementById('domain-count').textContent =
        `${domains.length} domaine(s)`;

    if (domains.length === 0) {
        tbody.innerHTML = `
            <tr><td colspan="4">
                <div class="empty-state">
                    <div class="icon">🚫</div>
                    <div>Aucun domaine bloqué</div>
                </div>
            </td></tr>`;
        return;
    }

    tbody.innerHTML = domains.map(d => `
        <tr>
            <td><strong>${d.domain}</strong></td>
            <td>${d.categoryName
                ? `<span class="badge badge-primary">${d.categoryName}</span>`
                : `<span class="badge badge-muted">—</span>`}
            </td>
            <td style="color:var(--text-muted)">${formatDate(d.createdAt)}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="askDelete('${d.id}')">🗑️ Supprimer</button>
            </td>
        </tr>
    `).join('');
}

function filterDomains() {
    const search   = document.getElementById('filter-domain').value.toLowerCase();
    const catId    = document.getElementById('filter-category').value;

    const filtered = allDomains.filter(d => {
        const matchDomain = d.domain.toLowerCase().includes(search);
        const matchCat    = !catId || d.categoryId === catId;
        return matchDomain && matchCat;
    });

    renderTable(filtered);
}

function openAddModal() {
    document.getElementById('input-domain').value = '';
    document.getElementById('input-category').value = '';
    openModal('modal-domain');
}

async function saveDomain() {
    const domain     = document.getElementById('input-domain').value.trim();
    const categoryId = document.getElementById('input-category').value || null;

    if (!domain) { showToast('Le domaine est obligatoire', 'error'); return; }

    try {
        await DomainAPI.create({ domain, categoryId });
        showToast('Domaine ajouté');
        closeModal('modal-domain');
        loadAll();
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

async function importDomains() {
    const file       = document.getElementById('import-file').files[0];
    const categoryId = document.getElementById('import-category').value || null;

    if (!file) { showToast('Sélectionne un fichier', 'error'); return; }

    const formData = new FormData();
    formData.append('file', file);
    if (categoryId) formData.append('categoryId', categoryId);

    try {
        const msg = await DomainAPI.import(formData);
        showToast(msg);
        closeModal('modal-import');
        loadAll();
    } catch (e) {
        showToast('Erreur import : ' + e.message, 'error');
    }
}

// Prévisualisation nombre de domaines dans le fichier
document.getElementById('import-file').addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const text  = await file.text();
    const count = text.split('\n').filter(l => l.trim()).length;
    document.getElementById('import-preview').textContent =
        `${count} domaine(s) détecté(s) dans le fichier`;
});

function askDelete(id) {
    document.getElementById('delete-id').value = id;
    openModal('modal-delete');
}

async function confirmDelete() {
    const id = document.getElementById('delete-id').value;
    try {
        await DomainAPI.delete(id);
        showToast('Domaine supprimé');
        closeModal('modal-delete');
        loadAll();
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

function formatDate(ts) {
    if (!ts) return '—';
    return new Date(ts).toLocaleDateString('fr-FR');
}

document.getElementById('filter-domain').addEventListener('input', filterDomains);
document.getElementById('filter-category').addEventListener('change', filterDomains);

loadAll();