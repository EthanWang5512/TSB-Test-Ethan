-- V5__LedgerEntries.sql
create table if not exists ledger_entries (
    id               bigint generated always as identity primary key,
    transfer_id      bigint not null references transfers(id) on delete cascade,
    account_id       bigint not null references accounts(id),
    direction        varchar(6) not null,                   -- DEBIT | CREDIT
    amount           numeric(19,4) not null,
    currency         char(3) not null,
    created_at       timestamptz not null default now(),
    reference        varchar(32),
    constraint ck_entry_dir      check (direction in ('DEBIT','CREDIT')),
    constraint ck_entry_amount   check (amount > 0),
    constraint ck_entry_ccy_len  check (char_length(currency) = 3)
);

create index if not exists idx_ledger_account_time on ledger_entries(account_id, created_at desc);
create index if not exists idx_ledger_transfer     on ledger_entries(transfer_id);
