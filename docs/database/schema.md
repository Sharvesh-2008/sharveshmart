# Database Schema

> **Status:** PHASE 1 · STEP 4 — Database Schema Design (docs only). No tables, migrations, or code exist yet.
> Sources of truth: `Problem_Statement.md`, `docs/diagrams/system-architecture.md`, `docs/diagrams/er-diagram.md`, `docs/diagrams/class-diagram.md`.

## 1. Database Overview

The Digital Products Marketplace uses **PostgreSQL 15** as its system of record. This document
defines the physical schema for the MVP: users/roles, product catalog, cart, orders, payments,
reviews, and purchase entitlements. Actual digital product files are **not** stored here — the
database keeps only metadata/references (`product_files`). Persistence later uses Spring Data JPA
+ Hibernate; this schema is the target those entities will map to.

## 2. Naming Conventions

- **Tables:** plural snake_case: `users`, `categories`, `products`, `product_files`, `carts`, `cart_items`, `orders`, `order_items`, `payments`, `reviews`, `purchase_entitlements`.
- **Columns:** snake_case: `id`, `created_at`, `updated_at`, `seller_id`, `category_id`, `product_id`, `order_id`, `user_id`.
- **PK:** `id BIGINT GENERATED ALWAYS AS IDENTITY`.
- **FK:** `{target_table_singular}_id` referencing the parent PK.
- **Timestamps:** `TIMESTAMPTZ` with `DEFAULT CURRENT_TIMESTAMP`.

## 3. Table Definitions

### users

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | User identifier |
| name | VARCHAR(100) | NO | | | Display name |
| email | VARCHAR(255) | NO | UNIQUE | | Login email |
| password_hash | VARCHAR(255) | NO | | | BCrypt hash |
| role | VARCHAR(20) | NO | | 'USER' | USER / SELLER / ADMIN |
| created_at | TIMESTAMPTZ | NO | | CURRENT_TIMESTAMP | Created time |

`CHECK (role IN ('USER','SELLER','ADMIN'))`

### categories

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | Category identifier |
| name | VARCHAR(100) | NO | UNIQUE | | Category name |
| description | TEXT | YES | | | Category description |

### products

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | Product identifier |
| seller_id | BIGINT | NO | FK→users.id | | Owning seller |
| category_id | BIGINT | NO | FK→categories.id | | Product category |
| title | VARCHAR(200) | NO | | | Listing title |
| description | TEXT | YES | | | Listing description |
| price | NUMERIC(10,2) | NO | | | Price (exact) |
| status | VARCHAR(20) | NO | | 'DRAFT' | Approval state |
| created_at | TIMESTAMPTZ | NO | | CURRENT_TIMESTAMP | Created time |
| updated_at | TIMESTAMPTZ | NO | | CURRENT_TIMESTAMP | Last modified |

`CHECK (price >= 0)`, `CHECK (status IN ('DRAFT','PENDING_APPROVAL','APPROVED','REJECTED','ARCHIVED'))`
Indexes: `(seller_id)`, `(category_id)`, `(status)`.

`updated_at` default initializes to `CURRENT_TIMESTAMP`; subsequent updates are managed by the
application/JPA layer, not by a PostgreSQL default or trigger.

### product_files

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | File record identifier |
| product_id | BIGINT | NO | FK→products.id | | Owning product |
| file_name | VARCHAR(255) | NO | | | Original file name |
| storage_reference | VARCHAR(255) | NO | UNIQUE | | External storage reference |
| file_type | VARCHAR(100) | NO | | | MIME/media type |
| file_size | BIGINT | NO | | | Bytes |
| created_at | TIMESTAMPTZ | NO | | CURRENT_TIMESTAMP | Created time |

`CHECK (file_size >= 0)`. Actual file contents are stored externally; only metadata here.

### carts

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | Cart identifier |
| user_id | BIGINT | NO | UNIQUE, FK→users.id | | Owner (one cart per user) |

### cart_items

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | Item identifier |
| cart_id | BIGINT | NO | FK→carts.id | | Containing cart |
| product_id | BIGINT | NO | FK→products.id | | Referenced product |
| quantity | INTEGER | NO | | 1 | Quantity |
| added_at | TIMESTAMPTZ | NO | | CURRENT_TIMESTAMP | Added time |

`UNIQUE (cart_id, product_id)`, `CHECK (quantity > 0)`.

### orders

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | Order identifier |
| user_id | BIGINT | NO | FK→users.id | | Buyer |
| total_amount | NUMERIC(12,2) | NO | | | Order total (exact) |
| status | VARCHAR(20) | NO | | 'PENDING' | PENDING/PAID/FAILED/CANCELLED |
| created_at | TIMESTAMPTZ | NO | | CURRENT_TIMESTAMP | Created time |

`CHECK (total_amount >= 0)`, `CHECK (status IN ('PENDING','PAID','FAILED','CANCELLED'))`
Indexes: `(user_id)`, `(status)`.

### order_items

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | Item identifier |
| order_id | BIGINT | NO | FK→orders.id | | Containing order |
| product_id | BIGINT | NO | FK→products.id | | Sold product |
| unit_price | NUMERIC(10,2) | NO | | | Historical price snapshot |
| quantity | INTEGER | NO | | | Quantity |

`CHECK (unit_price >= 0)`, `CHECK (quantity > 0)`. **Not** unique on `(order_id, product_id)`.
Index: `(order_id)`.

### payments

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | Payment identifier |
| order_id | BIGINT | NO | UNIQUE, FK→orders.id | | Settled order (0..1) |
| amount | NUMERIC(12,2) | NO | | | Amount (exact) |
| status | VARCHAR(20) | NO | | 'PENDING' | PENDING/SUCCESS/FAILED |
| method | VARCHAR(50) | NO | | 'MOCK' | Payment method |
| provider_reference | VARCHAR(255) | YES | | NULL | External provider ref (mock: nullable) |
| created_at | TIMESTAMPTZ | NO | | CURRENT_TIMESTAMP | Created time |
| paid_at | TIMESTAMPTZ | YES | | NULL | Completion time |

`CHECK (amount >= 0)`, `CHECK (status IN ('PENDING','SUCCESS','FAILED'))`.

### reviews

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | Review identifier |
| user_id | BIGINT | NO | FK→users.id | | Reviewer |
| product_id | BIGINT | NO | FK→products.id | | Reviewed product |
| rating | SMALLINT | NO | | | 1–5 |
| comment | TEXT | YES | | | Optional comment |
| created_at | TIMESTAMPTZ | NO | | CURRENT_TIMESTAMP | Created time |

`UNIQUE (user_id, product_id)`, `CHECK (rating BETWEEN 1 AND 5)`. Index: `(product_id)`.

### purchase_entitlements

| Column | Type | Null | Key | Default | Description |
|---|---|---|---|---|---|
| id | BIGINT | NO | PK | IDENTITY | Entitlement identifier |
| user_id | BIGINT | NO | FK→users.id | | Entitled user |
| product_id | BIGINT | NO | FK→products.id | | Purchased product |
| order_id | BIGINT | NO | FK→orders.id | | Creating order |
| granted_at | TIMESTAMPTZ | NO | | CURRENT_TIMESTAMP | Granted time |

`UNIQUE (user_id, product_id)`. Indexes: `(product_id)`, `(order_id)`.

## 4. Relationships

| From | To | FK column | Type |
|---|---|---|---|
| users | products | products.seller_id | 1→N (seller ownership) |
| categories | products | products.category_id | 1→N |
| products | product_files | product_files.product_id | 1→N |
| users | carts | carts.user_id | 1→1 (UNIQUE) |
| carts | cart_items | cart_items.cart_id | 1→N (composition) |
| products | cart_items | cart_items.product_id | 1→N |
| users | orders | orders.user_id | 1→N (buyer) |
| orders | order_items | order_items.order_id | 1→N (composition) |
| products | order_items | order_items.product_id | 1→N |
| orders | payments | payments.order_id | 1→0..1 (UNIQUE) |
| users | reviews | reviews.user_id | 1→N |
| products | reviews | reviews.product_id | 1→N |
| users | purchase_entitlements | purchase_entitlements.user_id | 1→N |
| products | purchase_entitlements | purchase_entitlements.product_id | 1→N |
| orders | purchase_entitlements | purchase_entitlements.order_id | 1→N |

## 5. Constraints

- **PK:** `id` identity on all 11 tables.
- **FK:** every FK listed above enforces referential integrity.
- **UNIQUE:** `users.email`, `categories.name`, `carts.user_id`, `cart_items(cart_id, product_id)`, `payments.order_id`, `reviews(user_id, product_id)`, `purchase_entitlements(user_id, product_id)`, `product_files.storage_reference`.
- **CHECK:** role/status value sets; `price >= 0`, `total_amount >= 0`, `amount >= 0`, `unit_price >= 0`; `quantity > 0`; `file_size >= 0`; `rating BETWEEN 1 AND 5`.
- **NOT NULL:** all identity/auth/catalog/order/payment/entitlement fields as shown in the tables.

## 6. Delete/Update Behavior

| FK | Behavior | Reasoning |
|---|---|---|
| products.seller_id → users.id | RESTRICT | Never orphan/drop a seller's listings |
| orders.user_id → users.id | RESTRICT | Protect transaction history |
| reviews.user_id / product_id | RESTRICT | Preserve review records |
| purchase_entitlements.user_id/product_id/order_id | RESTRICT | Preserve granted access |
| order_items.product_id → products.id | RESTRICT | Historical order integrity |
| categories.id → products.category_id | RESTRICT | Don't delete a category in use |
| carts.user_id → users.id | CASCADE | Cart is owned by the user |
| cart_items.cart_id / product_id | CASCADE | Transient line items |
| product_files.product_id → products.id | CASCADE | File metadata belongs to product |
| order_items.order_id → orders.id | CASCADE | Items are part of the order |
| payments.order_id → orders.id | CASCADE | Payment belongs to the order |

No `SET NULL` is used. Rationale: historical purchase/order information must not disappear when a
listing is deleted; only owned/transient child rows (carts, cart items, file metadata, order items,
payments) cascade. Product removal is modelled via `ARCHIVED` status, not destructive delete.

## 7. Index Strategy

Indexes that exist implicitly via UNIQUE constraints: `users.email`, `categories.name`,
`carts.user_id`, `cart_items(cart_id, product_id)`, `payments.order_id`, `reviews(user_id, product_id)`,
`purchase_entitlements(user_id, product_id)`, `product_files.storage_reference`.

Additional explicit indexes (query patterns):
- `products(seller_id)` — seller dashboard/ownership queries
- `products(category_id)` — catalog filtering
- `products(status)` — approval/moderation queries and catalog visibility
- `orders(user_id)` — buyer order history
- `orders(status)` — admin monitoring/order status
- `order_items(order_id)` — order detail joins
- `reviews(product_id)` — product review listing
- `purchase_entitlements(product_id)` — "who can access this product" / library checks
- `purchase_entitlements(order_id)` — entitlement creation/lookup by order

No duplicate or speculative indexes beyond these.

## 8. Status Representation

Role and status values use **`VARCHAR(20)` + `CHECK` constraints**, not PostgreSQL ENUM types and
not lookup tables:

| Field | Allowed values |
|---|---|
| users.role | USER, SELLER, ADMIN |
| products.status | DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, ARCHIVED |
| orders.status | PENDING, PAID, FAILED, CANCELLED |
| payments.status | PENDING, SUCCESS, FAILED |

**Why:** PostgreSQL ENUMs are rigid (adding a value requires `ALTER TYPE`); Java/JPA maps enum
classes to VARCHAR cleanly with `@Enumerated(EnumType.STRING)`; CHECK keeps invalid values out of
the DB while staying trivially maintainable. Four separate lookup tables are unnecessary for this
scope.

## 9. Money and Timestamp Strategy

- **Money:** exact `NUMERIC` only — `NUMERIC(10,2)` for `price`/`unit_price`, `NUMERIC(12,2)` for
  `total_amount`/`amount`. No floating-point types for money.
- **Timestamps:** `TIMESTAMPTZ` (TIMESTAMP WITH TIME ZONE) everywhere, default `CURRENT_TIMESTAMP`.
  `updated_at` appears only where genuinely needed (`products`); its default initializes it to
  `CURRENT_TIMESTAMP`, and subsequent updates are managed by the application/JPA layer (no
  PostgreSQL default or trigger). `paid_at` and `granted_at` are nullable completion stamps.

## 10. Consistency Check

- Same 11 tables/entities as `er-diagram.md` and `class-diagram.md` ✅
- Same relationships, multiplicities, uniqueness (incl. `payments.order_id` 0..1) ✅
- Same roles/statuses ✅ · same money/timestamp concepts ✅ · same entitlement-based access ✅
- No extra tables (no SellerProfile/Wishlist/DownloadLog/ledger/audit) ✅
- No contradictions found; previous documents were not modified.
