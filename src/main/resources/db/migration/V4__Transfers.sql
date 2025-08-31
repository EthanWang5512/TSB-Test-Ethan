-- V4__Transfers.sql
create table if not exists transfers (
    id                        bigint generated always as identity primary key,
    from_account_id           bigint not null references accounts(id),
    to_account_id             bigint not null references accounts(id),
    amount                    numeric(19,4) not null,
    currency                  char(3) not null default 'NZD',
    status                    varchar(16) not null,               -- PENDING | POSTED | FAILED | REVERSED | CANCELLED
    client_request_id         varchar(64) not null,               -- Used to prevent duplicate orders (unique constraint)
    reference                 varchar(255),
    schedule_date             timestamptz,
    frequency_code            varchar(16),                        -- NONE | DAILY | WEEKLY | MONTHLY | ...
    recurrence_end_date       date,
    created_by_user_id        bigint not null references users(id),
    processed_time            timestamptz not null default now(),
    posted_at                 timestamptz,
    created_at                timestamptz not null default now(),
    updated_at                timestamptz not null default now(),

    constraint ck_transfer_amount_pos check (amount > 0),
    constraint ck_transfer_ccy_len    check (char_length(currency) = 3),
    constraint ck_transfer_status     check (status in ('PENDING','POSTED','FAILED','REVERSED','CANCELLED')),
    constraint ck_transfer_freq       check (frequency_code is null 
                                             or frequency_code in ('NONE','DAILY','WEEKLY','MONTHLY')),
    constraint ck_transfer_not_self   check (from_account_id <> to_account_id),
    constraint uq_transfer_idem       unique (client_request_id )
);

create index if not exists idx_transfers_from        on transfers(from_account_id);
create index if not exists idx_transfers_to          on transfers(to_account_id);
create index if not exists idx_transfers_status      on transfers(status);
create index if not exists idx_transfers_created_by  on transfers(created_by_user_id, created_at desc);
