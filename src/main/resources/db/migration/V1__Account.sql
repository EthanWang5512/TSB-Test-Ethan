-- V1__Account.sql

create table if not exists customers (
    id                 bigint generated always as identity primary key,
    first_name         varchar(64) not null,
    last_name          varchar(64) not null,
    middle_name        varchar(64),
    preferred_name     varchar(64),
    dob                date,
    email              varchar(160),
    contact_number     varchar(32),
    address_line1   VARCHAR(200),
    address_line2   VARCHAR(200),
    suburb          VARCHAR(100),
    city            VARCHAR(100),
    region          VARCHAR(100),
    country         VARCHAR(100),
    postcode        VARCHAR(20),
    status             varchar(16) not null default 'ACTIVE', -- ACTIVE | SUSPENDED | CLOSED
    created_at         timestamptz not null default now(),
    updated_at         timestamptz not null default now(),
    constraint ck_customer_status check (status in ('ACTIVE','SUSPENDED','CLOSED'))
);

-- 账户表（允许多所有者 -> 见 account_owners）
create table if not exists accounts (
    id                 bigint generated always as identity primary key,
    account_number     varchar(32) not null unique,             -- e.g. 12-3274-0098125-00
    nickname           varchar(64) not null,
    account_type       varchar(32) not null,                    -- STREAMLINE | SAVINGS_ON_CALL | BUSINESS | FOREIGN_CURRENCY_CALL | TERM_DEPOSIT
    currency           char(3) not null default 'NZD',
    status             varchar(16) not null default 'ACTIVE',   -- ACTIVE | FROZEN | CLOSED
    balance            numeric(19,4) not null default 0,
    overdraft_limit    numeric(19,4) not null default 0,
    open_at            timestamptz not null default now(),
    close_at        timestamptz,
    updated_at         timestamptz not null default now(),
    paper_delivery                  BOOLEAN NOT NULL DEFAULT FALSE,
    resident_withholding_tax_rate   NUMERIC(5,2),
    ird_number                      VARCHAR(20),
    version                       BIGINT NOT NULL DEFAULT 0,
    constraint ck_account_ccy_len check (char_length(currency) = 3),
    constraint ck_account_status  check (status in ('ACTIVE','FROZEN','CLOSED')),
    constraint ck_account_type    check (account_type in ('STREAMLINE','SAVINGS_ON_CALL','BUSINESS','FOREIGN_CURRENCY_CALL','TERM_DEPOSIT')),
    constraint ck_balance_nonneg  check (balance >= 0 or overdraft_limit > 0)
);

-- 账户所有权（支持联合账户）
create table if not exists account_owners (
    account_id         bigint not null references accounts(id) on delete cascade,
    customer_id        bigint not null references customers(id) on delete cascade,
    owner_role         varchar(16) not null default 'PRIMARY',  -- PRIMARY | JOINT | TRUSTEE
    created_at         timestamptz not null default now(),
    primary key (account_id, customer_id),
    constraint ck_owner_role check (owner_role in ('PRIMARY','JOINT','TRUSTEE'))
);

create index if not exists idx_accounts_status on accounts(status);
create index if not exists idx_account_owners_cust on account_owners(customer_id);
