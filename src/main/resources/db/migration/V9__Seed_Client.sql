-- V9__Seed_Client.sql
-- Seed one client user + customer + 2 accounts (initial balance 1000)

-- 1) User
insert into users (username, email, phone_e164, status, is_email_verified, is_phone_verified)
values ('10000001', 'client@gmail.com', '+640212223333', 'ACTIVE', true, true)
    on conflict (username) do nothing;

-- Set password: 123456 (bcrypt)
insert into password_credentials (user_id, password_hash, password_algo, updated_at, password_updated_at)
select u.id,
       '{bcrypt}$2b$10$WPk5bS3/uIQZhIGbU88jO.uYgR7BytjB.AlMrb7Qow4pOfEJAxN9S', -- 123456
       'bcrypt',
       now(),
       now()
from users u
where u.username = '10000001'
    on conflict (user_id) do nothing;

-- 2) Customer basic info
-- customers has no unique constraint on (email), so use WHERE NOT EXISTS pattern
insert into customers (first_name, last_name, dob, email, contact_number,
                       address_line1, suburb, city, region, country, postcode, status)
select 'Alice', 'Client', '1990-01-01', 'client@gmail.com', '+640212223333',
       '123 Queen Street', 'Central', 'Auckland', 'Auckland', 'NZ', '1010', 'ACTIVE'
    where not exists (
    select 1 from customers c where c.email = 'client@gmail.com'
);

-- 3) Bind customer to user with ADMIN role
insert into customer_users (customer_id, user_id, access_role)
select c.id, u.id, 'ADMIN'
from customers c
         join users u on u.username = '10000001'
where c.email = 'client@gmail.com'
    on conflict (customer_id, user_id) do nothing;

-- 4) Two bank accounts (balance 1000 each)
insert into accounts (account_number, nickname, account_type, currency, status, balance)
values
    ('01-0980-0114667-19', 'Main Account',   'STREAMLINE',       'NZD', 'ACTIVE', 1000),
    ('01-1503-7127842-14', 'Savings Account','SAVINGS_ON_CALL',  'NZD', 'ACTIVE', 1000)
    on conflict (account_number) do nothing;

-- 5) Account ownership binding (PRIMARY)
insert into account_owners (account_id, customer_id, owner_role)
select a.id, c.id, 'PRIMARY'
from accounts a
         join customers c on c.email = 'client@gmail.com'
where a.account_number in ('01-0980-0114667-19','01-1503-7127842-14')
    on conflict (account_id, customer_id) do nothing;
