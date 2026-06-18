# WebControl — Moteur de Filtrage DNS et Classification des Risques

> Plateforme de contrôle et supervision de l'accès web en réseau domestique  
> Projet de fin d'année — 1ère année Ingénierie — ISGA Marrakech  
> Dépôt GitHub : https://github.com/Nepal667/WebControl.git

---

## Architecture du projet

```
Machine hôte (PC serveur)
│
├── Docker Container : Pi-hole       (port 53 UDP/TCP + port 8000 API interne)
├── Docker Container : PostgreSQL 15 (port 5432 interne)
│
├── Spring Boot (lancé directement sur la machine)  → port 8080
└── Nginx      (lancé directement sur la machine)  → port 80
```

> Pi-hole et PostgreSQL tournent dans Docker.  
> Spring Boot et le frontend HTML/CSS/JS sont servis directement sur la machine hôte.

---

## Prérequis

Avant de commencer, vérifier que les outils suivants sont installés sur la machine serveur :

| Outil | Version minimale | Vérification |
|---|---|---|
| Docker Desktop | 24+ | `docker --version` |
| Docker Compose | 2+ | `docker compose version` |
| Java (JDK) | 21 | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Git | 2+ | `git --version` |

---

## Étape 1 — Cloner le projet

```bash
git clone https://github.com/Nepal667/WebControl.git
cd WebControl
```

---

## Étape 2 — Créer le fichier de configuration `.env`

À la racine du projet, créer un fichier `.env` (jamais commité dans Git) :

```bash
# Copier le modèle fourni
cp .env.example .env
```

Contenu du fichier `.env` à adapter :

```env
# ─── PostgreSQL ────────────────────────────────────────
POSTGRES_DB=webcontrol
POSTGRES_USER=admin
POSTGRES_PASSWORD=ChangeMe2024!

# ─── Pi-hole ───────────────────────────────────────────
PIHOLE_WEBPASSWORD=ChangeMe2024!

# ─── JWT (clé secrète Spring Boot) ────────────────────
JWT_SECRET=3c8f2a1b9d4e7f6a2c5b8d1e4f7a0c3e6b9d2f5a8c1e4b7d0f3a6c9e2b5d8f1

# ─── URL de l'API Pi-hole (depuis Spring Boot) ─────────
PIHOLE_API_URL=http://localhost:8000/admin/api.php
PIHOLE_API_KEY=VOTRE_CLE_API_PIHOLE
```

> **Important :** La clé API Pi-hole est disponible dans l'interface admin de Pi-hole  
> après son démarrage, dans Settings → API / Web interface.

---

## Étape 3 — Lancer les deux containers Docker

### Le fichier `docker-compose.yml`

Voici le fichier Docker Compose à placer à la racine du projet :

```yaml
version: '3.8'

services:

  # ─────────────────────────────────────────────────────
  # Container 1 : Pi-hole — Moteur DNS filtrant
  # ─────────────────────────────────────────────────────
  pihole:
    image: pihole/pihole:latest
    container_name: pihole-webcontrol
    ports:
      - "53:53/tcp"       # Requêtes DNS (TCP)
      - "53:53/udp"       # Requêtes DNS (UDP)
      - "8000:80"         # Interface admin Pi-hole + API REST
    environment:
      WEBPASSWORD: ${PIHOLE_WEBPASSWORD}
      TZ: "Africa/Casablanca"
      DNSMASQ_LISTENING: "all"
      PIHOLE_DNS_: "8.8.8.8;8.8.4.4"
    volumes:
      - pihole-data:/etc/pihole
      - pihole-dnsmasq:/etc/dnsmasq.d
    networks:
      - dns-net
    restart: unless-stopped
    cap_add:
      - NET_ADMIN

  # ─────────────────────────────────────────────────────
  # Container 2 : PostgreSQL 15 — Base de données
  # ─────────────────────────────────────────────────────
  postgres:
    image: postgres:15
    container_name: postgres-webcontrol
    ports:
      - "5432:5432"       # Accessible depuis Spring Boot sur la machine hôte
    environment:
      POSTGRES_DB:       ${POSTGRES_DB}
      POSTGRES_USER:     ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - pg-data:/var/lib/postgresql/data
    networks:
      - dns-net
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5

# ─── Volumes persistants ───────────────────────────────
volumes:
  pg-data:
  pihole-data:
  pihole-dnsmasq:

# ─── Réseau interne ────────────────────────────────────
networks:
  dns-net:
    driver: bridge
```

### Lancer les containers

```bash
# Depuis la racine du projet
docker compose up -d
```

Vérifier que les deux containers sont bien démarrés :

```bash
docker ps
```

Résultat attendu :

```
CONTAINER ID   IMAGE                PORTS                                   NAMES
xxxxxxxxxxxx   pihole/pihole        0.0.0.0:53->53/tcp, 0.0.0.0:8000->80   pihole-webcontrol
xxxxxxxxxxxx   postgres:15          0.0.0.0:5432->5432/tcp                  postgres-webcontrol
```

---

## Étape 4 — Vérifier Pi-hole

Ouvrir un navigateur sur la machine serveur :

```
http://localhost:8000/admin
```

Se connecter avec le mot de passe défini dans `PIHOLE_WEBPASSWORD`.

### Récupérer la clé API Pi-hole

1. Aller dans **Settings → API / Web interface**
2. Cliquer sur **Show API token**
3. Copier le token et le coller dans le fichier `.env` à la ligne `PIHOLE_API_KEY=`

---

## Étape 5 — Vérifier PostgreSQL

Se connecter à la base de données pour confirmer qu'elle est opérationnelle.

Avec DBeaver (recommandé) :

```
Host     : localhost
Port     : 5432
Database : webcontrol
User     : admin
Password : ChangeMe2024!
```

Ou en ligne de commande :

```bash
docker exec -it postgres-webcontrol psql -U admin -d webcontrol -c "\dt"
```

> À ce stade la base est vide — les tables seront créées automatiquement par Flyway au démarrage de Spring Boot.

---

## Étape 6 — Configurer Spring Boot

Ouvrir le fichier `src/main/resources/application.yml` et vérifier les valeurs :

```yaml
spring:
  application:
    name: webcontrol

  datasource:
    url: jdbc:postgresql://localhost:5432/webcontrol
    username: admin
    password: ChangeMe2024!
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    show-sql: false
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

# ─── JWT ──────────────────────────────────────────────
app:
  jwt:
    secret: "3c8f2a1b9d4e7f6a2c5b8d1e4f7a0c3e6b9d2f5a8c1e4b7d0f3a6c9e2b5d8f1"
    access-token-expiration: 900000       # 15 minutes
    refresh-token-expiration: 604800000   # 7 jours

# ─── Pi-hole ──────────────────────────────────────────
  pihole:
    api-url: "http://localhost:8000/admin/api.php"
    api-key: "VOTRE_CLE_API_PIHOLE"

# ─── Actuator ─────────────────────────────────────────
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized

server:
  port: 8080

logging:
  level:
    com.schoolcontrol: DEBUG
    org.springframework.security: INFO
```

---

## Étape 7 — Vérifier les fichiers de migration SQL

S'assurer que les 3 fichiers Flyway sont présents dans :

```
src/main/resources/db/migration/
├── V1__init_schema.sql    ← création des tables
├── V2__indexes.sql        ← index de performance
└── V3__seed_data.sql      ← données initiales (super-admin, catégories, politiques)
```

---

## Étape 8 — Lancer Spring Boot

Depuis la racine du projet, avec Maven :

```bash
./mvnw spring-boot:run
```

Ou sur Windows (cmd) :

```cmd
mvnw.cmd spring-boot:run
```

### Logs attendus au démarrage

```
HikariPool-1 - Start completed.

Flyway Community Edition by Redgate
Database: jdbc:postgresql://localhost:5432/webcontrol (PostgreSQL 15)
Migrating schema "public" to version "1 - init schema"
Migrating schema "public" to version "2 - indexes"
Migrating schema "public" to version "3 - seed data"
Successfully applied 3 migrations to schema "public"

Started WebcontrolApplication in X.XXX seconds
```

> Si les 3 migrations s'affichent avec `Successfully applied` : tout fonctionne.

---

## Étape 9 — Vérifier que l'API répond

```bash
curl http://localhost:8080/actuator/health
```

Réponse attendue :

```json
{"status":"UP"}
```

### Tester le login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"Admin1234!"}'
```

Réponse attendue :

```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "username": "superadmin",
  "role": "ADMIN",
  "expiresIn": 900
}
```

> **Identifiants par défaut :** `superadmin` / `Admin1234!`  
> **À changer obligatoirement à la première connexion.**

---

## Étape 10 — Configurer le DNS du réseau

Pour que Pi-hole filtre les requêtes DNS de tous les appareils du réseau :

### Sur la machine cliente (PC test)

**Windows :**
1. `Win + R` → `ncpa.cpl`
2. Clic droit sur la carte réseau active → Propriétés
3. Protocole Internet version 4 → Propriétés
4. Serveur DNS préféré : `IP_DE_LA_MACHINE_SERVEUR`
5. Désactiver IPv6 : décocher "Protocole Internet version 6"

**Vider le cache DNS :**
```cmd
ipconfig /flushdns
```

**Désactiver DoH dans Firefox :**  
`about:preferences` → Paramètres réseau → décocher "Activer le DNS via HTTPS"

### Tester le blocage

```cmd
nslookup facebook.com IP_DE_LA_MACHINE_SERVEUR
```

Si le domaine est bloqué → réponse `0.0.0.0`  
Si autorisé → réponse avec l'adresse IP réelle

---

## Commandes utiles

```bash
# Voir les logs des containers en temps réel
docker logs -f pihole-webcontrol
docker logs -f postgres-webcontrol

# Arrêter les containers
docker compose down

# Arrêter et supprimer les volumes (remet la BD à zéro)
docker compose down -v

# Redémarrer un container
docker restart pihole-webcontrol
docker restart postgres-webcontrol

# Vérifier l'état des containers
docker ps

# Accéder au shell PostgreSQL
docker exec -it postgres-webcontrol psql -U admin -d webcontrol

# Lister les tables créées par Flyway
docker exec -it postgres-webcontrol psql -U admin -d webcontrol -c "\dt"
```

---

## Structure du projet

```
WebControl/
│
├── docker-compose.yml              ← orchestration Pi-hole + PostgreSQL
├── .env                            ← variables d'environnement (non commité)
├── .env.example                    ← modèle à copier
│
├── src/
│   └── main/
│       ├── java/com/schoolcontrol/webcontrol/
│       │   ├── controller/         ← endpoints REST
│       │   ├── service/            ← logique métier
│       │   ├── repository/         ← accès base de données (JPA)
│       │   ├── entity/             ← entités JPA (tables BD)
│       │   ├── dto/                ← objets de transfert
│       │   ├── security/           ← JWT + Spring Security
│       │   ├── config/             ← configuration Spring
│       │   └── scheduler/          ← jobs automatiques (polling Pi-hole)
│       │
│       └── resources/
│           ├── application.yml     ← configuration Spring Boot
│           └── db/migration/
│               ├── V1__init_schema.sql
│               ├── V2__indexes.sql
│               └── V3__seed_data.sql
│
└── frontend/                       ← pages HTML/CSS/JS
    ├── index.html                  ← dashboard
    ├── login.html                  ← page de connexion
    ├── journal.html                ← journal DNS
    ├── domaines.html               ← gestion des blocages
    ├── politiques.html             ← gestion des politiques
    ├── rapports.html               ← rapports et exports
    ├── css/
    └── js/
```

---

## Résolution des problèmes courants

**Le port 53 est déjà utilisé (Windows)**
```powershell
# Désactiver le service DNS de Windows si actif
net stop "DNS Client"
# Ou changer le port dans docker-compose.yml : "5353:53/udp"
```

**Spring Boot ne se connecte pas à PostgreSQL**
- Vérifier que le container PostgreSQL est bien démarré : `docker ps`
- Vérifier les identifiants dans `application.yml`
- Vérifier que le port 5432 est bien exposé : `docker ps | grep 5432`

**Flyway échoue au démarrage**
- Vérifier que les 3 fichiers SQL sont dans `src/main/resources/db/migration/`
- Vérifier que les noms respectent le format `V{n}__{description}.sql`
- En dernier recours, supprimer la base et recommencer : `docker compose down -v`

**Pi-hole ne reçoit pas les requêtes DNS**
- Vérifier que le DNS du PC client pointe vers l'IP de la machine serveur
- Vérifier que IPv6 est désactivé sur le PC client
- Vérifier que DoH est désactivé dans le navigateur

---

## Comptes par défaut

| Compte | Identifiant | Mot de passe | Rôle |
|---|---|---|---|
| Application WebControl | `superadmin` | `Admin1234!` | ADMIN |
| Interface Pi-hole admin | — | `ChangeMe2024!` | — |
| Base de données PostgreSQL | `admin` | `ChangeMe2024!` | — |

> Changer tous ces mots de passe avant toute mise en production.

---

## Liens utiles

| Service | URL |
|---|---|
| API Spring Boot | http://localhost:8080 |
| Santé de l'API | http://localhost:8080/actuator/health |
| Interface admin Pi-hole | http://localhost:8000/admin |
| Frontend (avec Nginx) | http://localhost:80 |

---

*WebControl — ISGA Marrakech — Projet fin d'année 2025/2026*
