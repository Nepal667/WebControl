CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE dns_status AS ENUM ('ALLOWED', 'BLOCKED');

CREATE TABLE role (
    id   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE admin (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username      VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id       UUID NOT NULL REFERENCES role(id),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE category (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE policy (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE policy_category (
    policy_id   UUID NOT NULL REFERENCES policy(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    PRIMARY KEY (policy_id, category_id)
);

CREATE TABLE blocked_domain (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    domain      VARCHAR(255) UNIQUE NOT NULL,
    category_id UUID REFERENCES category(id) ON DELETE SET NULL,
    policy_id   UUID REFERENCES policy(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE dns_log (
    id            BIGSERIAL PRIMARY KEY,
    timestamp     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    domain        VARCHAR(255) NOT NULL,
    client_ip     INET NOT NULL,
    status        dns_status NOT NULL,
    response_time INT
);

CREATE TABLE dns_log_daily_stats (
    id               BIGSERIAL PRIMARY KEY,
    stat_date        DATE NOT NULL,
    domain           VARCHAR(255) NOT NULL,
    total_requests   INT NOT NULL DEFAULT 0,
    blocked_requests INT NOT NULL DEFAULT 0,
    UNIQUE (stat_date, domain)
);

CREATE TABLE report (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    type         VARCHAR(100) NOT NULL,
    file_path    VARCHAR(500),
    period_start TIMESTAMPTZ,
    period_end   TIMESTAMPTZ,
    admin_id     UUID REFERENCES admin(id) ON DELETE SET NULL
);