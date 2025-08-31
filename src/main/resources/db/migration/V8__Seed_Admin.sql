-- V8__Seed_Admin.sql
-- Create super administrator: username=admin, password=123456 (bcrypt)
-- Also create SYSADMIN role, common permissions, and grant all permissions.

-- 1) Roles & Permissions
insert into roles(code, name, scope) values
  ('SYSADMIN','System Administrator', 'PLATFORM')
on conflict (code) do nothing;

insert into permissions(code, name) values
  ('MANAGE_ROLES','Manage roles & permissions'),
  ('VIEW_AUDIT','View audit logs'),
  ('MANAGE_USERS','Manage platform users'),
  ('RESET_PASSWORD','Reset any user password'),
  ('MANAGE_ACCOUNTS','Open/close accounts'),
  ('TRANSFER_FUNDS','Perform/internal transfers'),
  ('VIEW_USERS','View users'),
  ('VIEW_ACCOUNTS','View accounts')
on conflict (code) do nothing;

-- Grant all permissions to SYSADMIN
insert into role_permissions(role_id, permission_id)
select r.id, p.id
from roles r
cross join permissions p
where r.code = 'SYSADMIN'
  and not exists (
    select 1 from role_permissions rp where rp.role_id = r.id and rp.permission_id = p.id
  );

-- 2) admin user
insert into users(username, email, phone_e164, status, is_email_verified, is_phone_verified)
values ('admin', null, null, 'ACTIVE', true, true)
on conflict do nothing;



-- 3) Set password (bcrypt).
insert into password_credentials(user_id, password_hash, password_algo, updated_at)
select u.id,
       '{bcrypt}$2b$10$WPk5bS3/uIQZhIGbU88jO.uYgR7BytjB.AlMrb7Qow4pOfEJAxN9S',
       'bcrypt',
       now()
from users u
where u.username = 'admin'
on conflict (user_id) do update set
  password_hash = excluded.password_hash,
  password_algo = excluded.password_algo,
  updated_at    = excluded.updated_at;

-- 4) Assign SYSADMIN role
insert into user_roles(user_id, role_id)
select u.id, r.id
from users u
join roles r on r.code = 'SYSADMIN'
where u.username = 'admin'
  and not exists (
    select 1 from user_roles ur where ur.user_id = u.id and ur.role_id = r.id
  );

-- 5) Create staff profile (treat admin as an employee)
insert into staff_profiles(user_id, employee_no, branch_code, title, status)
select u.id, 'EMP-0001', 'HQ', 'Administrator', 'ACTIVE'
from users u
where u.username = 'admin'
on conflict (user_id) do nothing;
