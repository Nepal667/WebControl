let policies   = [];
let categories = [];

async function loadAll() {
    try {
        [policies, categories] = await Promise.all([
            PolicyAPI.getAll(),
            CategoryAPI.getAll()
        ]);
        renderGrid();
        renderCheckboxes([]);
    } catch (e) {
        showToast('Erreur chargement : ' + e.message, 'error');
    }
}

function renderGrid() {
    const grid = document.getElementById('policies-grid');

    if (policies.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <div class="icon">📜</div>
                <div>Aucune politique définie</div>
            </div>`;
        return;
    }

    grid.innerHTML = policies.map(p => `
        <div class="card" style="margin-bottom:0">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px;">
                <div>
                    <div style="font-size:16px;font-weight:700">${p.name}</div>
                    <div style="font-size:13px;color:var(--text-muted);margin-top:4px">${p.description || '—'}</div>
                </div>
                ${p.isActive
                    ? `<span class="badge badge-success">Active</span>`
                    : `<span class="badge badge-muted">Inactive</span>`}
            </div>

            <div style="margin-bottom:16px;">
                <div style="font-size:12px;color:var(--text-muted);margin-bottom:6px;">CATÉGORIES</div>
                <div style="display:flex;flex-wrap:wrap;gap:4px;">
                    ${p.categories && p.categories.length > 0
                        ? p.categories.map(c =>
                            `<span class="badge badge-primary">${c.name}</span>`).join('')
                        : `<span class="badge badge-muted">Aucune</span>`}
                </div>
            </div>

            <div style="display:flex;gap:8px;flex-wrap:wrap;">
                ${!p.isActive ? `
                    <button class="btn btn-success btn-sm" onclick="activatePolicy('${p.id}')">▶️ Activer</button>
                ` : ''}
                <button class="btn btn-outline btn-sm" onclick="editPolicy('${p.id}')">✏️ Modifier</button>
                <button class="btn btn-danger btn-sm" onclick="askDelete('${p.id}')">🗑️ Supprimer</button>
            </div>
        </div>
    `).join('');
}

function renderCheckboxes(selectedIds) {
    const container = document.getElementById('category-checkboxes');
    container.innerHTML = categories.map(c => `
        <label style="display:flex;align-items:center;gap:8px;cursor:pointer;font-size:14px;">
            <input type="checkbox" value="${c.id}"
                ${selectedIds.includes(c.id) ? 'checked' : ''}>
            ${c.name}
        </label>
    `).join('');
}

function openAddModal() {
    document.getElementById('modal-title').textContent = 'Nouvelle politique';
    document.getElementById('edit-id').value = '';
    document.getElementById('input-name').value = '';
    document.getElementById('input-description').value = '';
    renderCheckboxes([]);
    openModal('modal-policy');
}

function editPolicy(id) {
    const p = policies.find(x => x.id === id);
    if (!p) return;
    document.getElementById('modal-title').textContent = 'Modifier la politique';
    document.getElementById('edit-id').value = p.id;
    document.getElementById('input-name').value = p.name;
    document.getElementById('input-description').value = p.description || '';
    const selectedIds = (p.categories || []).map(c => c.id);
    renderCheckboxes(selectedIds);
    openModal('modal-policy');
}

async function savePolicy() {
    const id          = document.getElementById('edit-id').value;
    const name        = document.getElementById('input-name').value.trim();
    const description = document.getElementById('input-description').value.trim();
    const categoryIds = [...document.querySelectorAll('#category-checkboxes input:checked')]
                        .map(cb => cb.value);

    if (!name) { showToast('Le nom est obligatoire', 'error'); return; }

    try {
        if (id) {
            await PolicyAPI.update(id, { name, description, categoryIds });
            showToast('Politique mise à jour');
        } else {
            await PolicyAPI.create({ name, description, categoryIds });
            showToast('Politique créée');
        }
        closeModal('modal-policy');
        loadAll();
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

async function activatePolicy(id) {
    try {
        await PolicyAPI.activate(id);
        showToast('Politique activée');
        loadAll();
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

function askDelete(id) {
    document.getElementById('delete-id').value = id;
    openModal('modal-delete');
}

async function confirmDelete() {
    const id = document.getElementById('delete-id').value;
    try {
        await PolicyAPI.delete(id);
        showToast('Politique supprimée');
        closeModal('modal-delete');
        loadAll();
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

loadAll();