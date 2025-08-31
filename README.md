# TSB Banking Demo

A lightweight demo of a banking backend/API tailored for the New Zealand market. Built with **Spring Boot 3**, **MyBatis / MyBatis-Plus**, **PostgreSQL (AWS RDS)**, and **JWT** (Access/Refresh token). The system demonstrates internal NZD transfers, concurrency control, and double-entry accounting.

---

## 1. Running the Project

### Requirements
- Docker / Docker Desktop
- PostgreSQL (configured on AWS RDS; connection is hardcoded in `application.yml` for demo purposes)
- Port: `8080`

### Start
```bash
docker compose up --build
```

- The build stage uses Maven to package the JAR.
- The run stage uses `eclipse-temurin:21-jre`.
- Runs with `dev` profile, timezone `Pacific/Auckland`.

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)

    

---

## 2. Notes

- **Database credentials are stored in `application.yml`** to simplify setup for interviewers. **Do not use this configuration in production**. Proper setups must use environment variables or secret managers.
- **Password Reset Mock**: `/api/users/request-reset-password-otp` is implemented without Twilio. It simply prints the OTP to the console for demo. The response does not include the OTP.
- **Email Verification Mock**: `/api/users/send-verify-email` is triggered after a new user registration to simulate sending a verification email and initializing the password. This is also mocked and not integrated with a real mail provider.
- **Login Channels**: There are two login domains: **Retail** and **Staff**. Each requires a different token to access the respective APIs. This distinction is documented in both Postman and Swagger collections.

---

## 3. Testing with Postman / Swagger

### Postman
1. Import the collection (`TSB.postman_collection.json`).
2. Login using seeded accounts (admin or client).
3. Configure headers:
   - `Authorization: Bearer <accessToken>`
   - `X-Refresh-Token: <refreshToken>`
4. Test scenarios:
   - Query accounts.
   - Execute a transfer.
   - List transfers & ledger entries.

### Swagger
- Provides interactive API docs and example requests.

---

## 4. Database Design (Flyway Migrations)

The database schema is designed to mimic realistic banking requirements, with Flyway migrations under `db/migration/`.

### Core Concepts
- **Three-layer structure:**
  - **User**: login identity, authentication and sessions.
  - **Customer**: legal/real-world entity that owns accounts.
  - **Account**: financial accounts tied to customers via ownership.
- **Transfers & Ledger Entries:**
  - Every transfer records a movement of funds.
  - Ledger entries enforce **double-entry accounting**: for each transfer, one DEBIT and one CREDIT, ensuring balance and auditability.

### V1 – Accounts & Customers
- `customers`, `accounts`, `account_owners`.
- Supports joint accounts and multiple owners.

### V2 – Authentication & Binding
- `users`, `password_credentials`, `sessions`, `user_tokens`, `auth_events`.
- `customer_users` joins customers and users, with access roles (`VIEW_ONLY`, `TRANSACT`, `ADMIN`).

### V3 – Roles & Staff Profiles
- `roles`, `permissions`, `role_permissions`, `user_roles`, `staff_profiles`.
- Separates platform roles (e.g., SYSADMIN) from retail roles.

### V4 – Transfers
- `transfers` table stores transfer requests.
- Enforces idempotency, currency consistency, and prevents self-transfers.

### V5 – Ledger Entries
- `ledger_entries` implements **double-entry accounting** (DEBIT & CREDIT for each transfer).
- Enables reconciliation and audit.

### V7 – Double-Entry Constraint
- Constraint trigger ensures each transfer has exactly 2 ledger entries, balanced, and same currency.

### V8 – Seed Admin
- Creates `SYSADMIN` role with full permissions.
- Inserts demo administrator user.

### V9 – Seed Client
- Seeds one demo customer, user, and two accounts (Main + Savings).
- Default password `123456` (bcrypt).

Additionally, see `Database_Design.pny` in the project root for the full XML diagram.

---

## 5. Core Business & Security Features

### Token & Session Management
- **Access Token**: Short-lived JWT, passed in `Authorization: Bearer <token>`.
- **Refresh Token**: Long-lived, stored server-side as hash, passed in `X-Refresh-Token` header.
- Audit logs recorded in `auth_events`.

### Internal Transfer Logic
The transfer service (`createInternalTransfer`) implements:
- Permission checks: debit/credit rights validated per user.
- Idempotency: `clientRequestId` prevents duplicate transfers.
- Validations: account status, same-currency, non-self-transfer.
- Concurrency: optimistic locking + retry ensures atomic debit/credit.
- Double-entry: inserts matching `ledger_entries` (DEBIT & CREDIT).
- Finalization: updates transfer status to `POSTED`.

---

## 6. Project Structure
```
.
├─ src/main/java/...          # Controllers, services, mappers, entities
├─ src/main/resources/
│  ├─ application.yml         # Hardcoded AWS RDS connection (demo only)
│  ├─ db/migration/           # Flyway SQL scripts (V1..V9)
│  └─ mapper/*.xml            # MyBatis / MyBatis-Plus custom SQL
├─ Dockerfile
├─ docker-compose.yml
└─ TSB.postman_collection.json
```

---

## 7. FAQ

- **Why only NZD?**
  Focused on internal transfer and consistency demo for NZ retail banking.

- **Why separate customers and users?**
  Banking practice: one natural person may have multiple identities (staff + retail). Separation ensures least privilege and compliance.

- **Why enforce double-entry in DB?**
  Guarantees accounting balance at persistence layer, not just in application logic.

---

## 9. Contact
If you have any questions or feedback, please feel free to contact me at **ethan.wang.5512@gmail.com**

