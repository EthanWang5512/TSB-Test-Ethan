-- V3__Roles.sql

create table if not exists roles (
    id                 bigint generated always as identity primary key,
    code               varchar(64) not null unique,                -- e.g. SYSADMIN, TELLER, BRANCH_MANAGER
    name               varchar(128) not null,
    scope              varchar(16)  not null default 'NULL',
    created_at         timestamptz not null default now(),
    constraint ck_scope check (scope in ('PLATFORM','RETAIL', 'NULL'))
);

create table if not exists permissions (
    id                 bigint generated always as identity primary key,
    code               varchar(64) not null unique,                -- e.g. MANAGE_ROLES, VIEW_AUDIT, RESET_PASSWORD
    name               varchar(128) not null
);

create table if not exists role_permissions (
    role_id            bigint not null references roles(id) on delete cascade,
    permission_id      bigint not null references permissions(id) on delete cascade,
    primary key (role_id, permission_id)
);

create table if not exists user_roles (
    user_id            bigint not null references users(id) on delete cascade,
    role_id            bigint not null references roles(id) on delete cascade,
    primary key (user_id, role_id)
);

create table if not exists staff_profiles (
    id                 bigint generated always as identity primary key,
    user_id            bigint not null unique references users(id) on delete cascade,
    employee_no        varchar(32) not null unique,
    branch_code        varchar(16),
    title              varchar(64),
    status             varchar(16) not null default 'ACTIVE',      -- ACTIVE | SUSPENDED | LEFT
    created_at         timestamptz not null default now(),
    constraint ck_staff_status check (status in ('ACTIVE','SUSPENDED','LEFT'))
);

create index if not exists idx_user_roles_user on user_roles(user_id);
create index if not exists idx_staff_profiles_user on staff_profiles(user_id);
