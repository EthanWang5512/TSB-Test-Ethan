-- V2__Auth.sql

create table if not exists users (
    id                 bigint generated always as identity primary key,
    username           varchar(64),
    email              varchar(160),
    phone_e164         varchar(32),
    status             varchar(16) not null default 'ACTIVE',     -- ACTIVE | LOCKED | DISABLED
    is_email_verified  boolean not null default false,
    is_phone_verified  boolean not null default false,
    created_at         timestamptz not null default now(),
    updated_at         timestamptz not null default now(),
    last_login_at      timestamptz not null default now(),
    constraint uq_user_email unique (email),
    constraint uq_user_phone unique (phone_e164),
    constraint uq_user_username unique (username),
    constraint ck_user_status check (status in ('ACTIVE','LOCKED','DISABLED'))
);

CREATE TABLE IF NOT EXISTS password_credentials (
    user_id             BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    password_hash       TEXT,
    password_algo       VARCHAR(32) NOT NULL DEFAULT 'bcrypt',
    updated_at          TIMESTAMPTZ,
    password_updated_at TIMESTAMPTZ,
    failed_login_count  INT NOT NULL DEFAULT 0,
    locked_until        TIMESTAMPTZ
    );

create table if not exists sessions (
    id                 bigint generated always as identity primary key,
    user_id            bigint not null references users(id) on delete cascade,
    scope              varchar(16) not null default 'NULL',
    refresh_token_hash varchar(128) not null,
    expires_at         timestamptz not null,
    ip_address         inet,
    user_agent         varchar(255),
    created_at         timestamptz not null default now(),
    revoked_at          timestamptz,
    constraint idx_uq_refresh unique (refresh_token_hash),
    constraint ck_scope check (scope in ('PLATFORM','RETAIL','NULL'))
);

create table if not exists user_tokens (
    id                 bigint generated always as identity primary key,
    user_id            bigint not null references users(id) on delete cascade,
    purpose            varchar(32) not null,                      -- PASSWORD_RESET | VERIFY_EMAIL | MFA | DEVICE_BIND
    token_hash         varchar(128) not null,
    expires_at         timestamptz not null,
    used_at            timestamptz,
    created_at         timestamptz not null default now()
);

create table if not exists auth_events (
    id                 bigint generated always as identity primary key,
    user_id            bigint references users(id) on delete set null,
    event_type         varchar(32) not null,                      -- LOGIN | LOGOUT | MFA_SUCCESS | MFA_FAILURE | PASSWORD_RESET | REGISTER | TOKEN_REFRESH
    ip_address         inet,
    user_agent         text,
    meta               jsonb not null default '{}'::jsonb,
    created_at         timestamptz not null default now()
);

-- 现在在 V2 中创建 customer_users（依赖 customers@V1 与 users@V2 均已存在）
create table if not exists customer_users (
    id                 bigint generated always as identity primary key,
    customer_id        bigint not null references customers(id) on delete cascade,
    user_id            bigint not null references users(id),
    access_role        varchar(16) not null default 'VIEW_ONLY',        -- VIEW_ONLY | TRANSACT | ADMIN
    created_at         timestamptz not null default now(),
    constraint uq_customer_user unique (customer_id, user_id),
    constraint ck_cu_role check (access_role in ('VIEW_ONLY','TRANSACT','ADMIN'))
);

create index if not exists idx_sessions_user on sessions(user_id);
create index if not exists idx_user_tokens_user on user_tokens(user_id);
create index if not exists idx_auth_events_user_time on auth_events(user_id, created_at desc);
