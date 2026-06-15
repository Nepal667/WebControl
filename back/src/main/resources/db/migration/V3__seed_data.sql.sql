-- Roles
INSERT INTO role (id, name) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'SUPER_ADMIN'),
    ('a0000000-0000-0000-0000-000000000002', 'ADMIN');

-- Super admin par défaut  (mot de passe : Admin1234!)
INSERT INTO admin (id, username, password_hash, role_id, is_active) VALUES
    (
        'b0000000-0000-0000-0000-000000000001',
        'superadmin',
        '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
        'a0000000-0000-0000-0000-000000000001',
        TRUE
    );

-- Catégories de base
INSERT INTO category (id, name, description, is_active) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Malware & Phishing',      'Sites malveillants et tentatives de phishing',         TRUE),
    ('c0000000-0000-0000-0000-000000000002', 'Publicités & Trackers',   'Réseaux publicitaires et scripts de tracking',         TRUE),
    ('c0000000-0000-0000-0000-000000000003', 'Jeux d''argent',          'Casinos en ligne et paris sportifs',                   FALSE),
    ('c0000000-0000-0000-0000-000000000004', 'Contenu adulte',          'Sites à caractère explicite',                          FALSE),
    ('c0000000-0000-0000-0000-000000000005', 'Réseaux sociaux',         'Facebook, TikTok, Instagram, etc.',                    FALSE),
    ('c0000000-0000-0000-0000-000000000006', 'Streaming vidéo',         'YouTube, Netflix, Twitch, etc.',                       FALSE);

-- Politique par défaut
INSERT INTO policy (id, name, description, is_active) VALUES
    (
        'd0000000-0000-0000-0000-000000000001',
        'Politique par défaut',
        'Bloque les malwares et publicités — recommandé pour tous les réseaux',
        TRUE
    );

-- Association politique ↔ catégories actives par défaut
INSERT INTO policy_category (policy_id, category_id) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001'),
    ('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002');