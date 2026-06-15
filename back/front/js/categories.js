let categories = [];

async function loadCategories() {
    try {
        categories = await CategoryAPI.getAll();
        renderTable();
    } catch (e) {
        showToast('Erreur chargement : ' + e.message, 'error');
    }
}

function renderTable() {
    const tbody = document.getElementById('categories-body');

    if (categories.length === 0) {
        tbody.innerHTML = `
            <tr><td colspan="4">
                <div class="empty-state">
                    <div class="icon">🏷️</div>
                    <div>Aucune catégorie trouvée</div>
                </div>
            </td></tr>`;
        return;
    }

    tbody.innerHTML = categories.map(cat => `
        <tr>
            <td><strong>${cat.name}</strong></td>
            <td style="color:var(--text-muted)">${cat.description || '—'}</td>
            <td>${cat.isActive
                ? `<span class="badge badge-success">Active</span>`
                : `<span class="badge badge-muted">Inactive</span>`}
            </td>
            <td>
                <div style="display:flex;gap:6px;">
                    <button class="btn btn-outline btn-sm" onclick="editCategory('${cat.id}')">✏️ Modifier</button>
                    <button class="btn btn-outline btn-sm" onclick="toggleCategory('${cat.id}')">
                        ${cat.isActive ? '⏸️ Désactiver' : '▶️ Activer'}
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="askDelete('${cat.id}')">🗑️ Supprimer</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openAddModal() {
    document.getElementById('modal-title').textContent = 'Nouvelle catégorie';
    document.getElementById('edit-id').value = '';
    document.getElementById('input-name').value = '';
    document.getElementById('input-description').value = '';
    openModal('modal-category');
}

function editCategory(id) {
    const cat = categories.find(c => c.id === id);
    if (!cat) return;
    document.getElementById('modal-title').textContent = 'Modifier la catégorie';
    document.getElementById('edit-id').value = cat.id;
    document.getElementById('input-name').value = cat.name;
    document.getElementById('input-description').value = cat.description || '';
    openModal('modal-category');
}

async function saveCategory() {
    const id   = document.getElementById('edit-id').value;
    const name = document.getElementById('input-name').value.trim();
    const desc = document.getElementById('input-description').value.trim();

    if (!name) { showToast('Le nom est obligatoire', 'error'); return; }

    try {
        if (id) {
            await CategoryAPI.update(id, { name, description: desc });
            showToast('Catégorie mise à jour');
        } else {
            await CategoryAPI.create({ name, description: desc });
            showToast('Catégorie créée');
        }
        closeModal('modal-category');
        loadCategories();
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

async function toggleCategory(id) {
    try {
        await CategoryAPI.toggle(id);
        showToast('Statut mis à jour');
        loadCategories();
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
        await CategoryAPI.delete(id);
        showToast('Catégorie supprimée');
        closeModal('modal-delete');
        loadCategories();
    } catch (e) {
        showToast('Erreur : ' + e.message, 'error');
    }
}

// Correction du bouton "Nouvelle catégorie" dans le topbar
document.querySelector('.btn-primary').addEventListener('click', openAddModal);

loadCategories();