# Sharvesh Mart

An online platform for buying and selling **digital products** — ebooks, software, digital
templates, design assets, and courses. The system combines a structured product catalog with a
controlled transaction flow so that buyers can discover, purchase, and download digital products,
while sellers manage their own listings under admin moderation.

```
Product → Cart → Checkout → Payment → Order → PurchaseEntitlement → Digital Library → Authorized Download
```

## Project Overview

The marketplace is a **full-stack, modular-monolith MVC application**:

- A **Spring Boot REST backend** that owns all business rules: product availability, cart and
  order totals, transaction status, purchase ownership, and download authorization. The backend is
  the single source of truth for security and authorization.
- A **React single-page application** that consumes the REST API with JWT authentication.
- **Controlled digital access**: a user can never download a product merely by guessing a URL —
  access is granted only after the backend verifies the authenticated user's valid **purchase
  entitlement**.

## Problem Solved

Independent creators and sellers of digital products lack a simple, centralized way to sell their
work with automated, controlled delivery. Manual payment and file-transfer processes are
fragmented; buyers have no trusted place to discover verified products; and digital delivery
requires controlled access after a successful purchase. This marketplace solves that by combining
**discovery → transaction → purchase tracking → authorized download** in one workflow.

## Features

### Authentication & Authorization
- Register / login with username-style email + password
- Stateless **JWT** authentication (HS256, JJWT)
- Passwords hashed with **BCrypt**
- Role-based authorization: **USER**, **SELLER**, **ADMIN** (`@PreAuthorize`)
- 401 / 403 JSON responses for missing, invalid, or unauthorized requests

### Product Catalog (public)
- Approved products list (`GET /api/products`), optional category filter
- Product details for approved products
- Seller product listings (public, all statuses)
- Category listing

### Seller
- Create draft product listings
- Update / archive own products only (server-side ownership checks)
- Submit products for admin approval (`PENDING_APPROVAL`)

### Admin Moderation
- List pending products
- Approve / reject products

### Buying (backend API; Swagger-demonstrable)
- Cart: add / update / remove items, view cart (USER only)
- Checkout: creates a `PENDING` order and clears the cart
- Payment: mock provider (`MOCK-<uuid>`), transitions order to `PAID`/`FAILED`
- Digital Library: list purchased products
- Download authorization: returns file metadata only after entitlement is verified

### Reviews
- Verified buyers can create / update / delete reviews (rating 1–5, one per product per user)
- Public review listing per product

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite 8, Tailwind CSS 4, Axios, React Router 7 |
| Backend | Spring Boot 3.5, Java 17, Maven, Spring MVC |
| Persistence | Spring Data JPA, Hibernate |
| Security | Spring Security, JWT (JJWT 0.12), BCrypt |
| Database | PostgreSQL 15 (local development and production) |
| API docs | springdoc-openapi, Swagger UI |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, Mockito, Spring Boot Test, MockMvc |
| CI | GitHub Actions |

## Architecture Summary

- **Modular monolith**: a single stateless Spring Boot API serves the React SPA.
- **Layered backend**: `Controller (DTO + validation) → Service (business logic) → Repository
  (Spring Data JPA) → Database`.
- **Stateless security**: Spring Security validates a signed JWT on every request and populates the
  security context; no server-side sessions.
- **Payment abstraction**: `PaymentProcessor` interface with a `MockPaymentProcessor`
  implementation — a real gateway can be swapped in later without redesign.
- **File storage abstraction**: only file *metadata* is stored in the database; actual files live
  in external storage referenced by `storageReference`, which is never exposed through APIs.
- **Purchase entitlement**: a successful, verified payment grants a `PurchaseEntitlement` row per
  purchased product — the basis of the Digital Library and download authorization.

## User Roles

| Role | Capabilities |
|---|---|
| **USER** (buyer) | Browse catalog, manage cart, checkout, pay, view orders, access Digital Library, download authorized products, review purchased products |
| **SELLER** | Everything a USER can do, plus create / update / archive **own** products and submit them for approval |
| **ADMIN** | Everything above, plus moderation: list pending products, approve / reject |

Role boundaries are enforced server-side. Users cannot reach seller or admin endpoints; sellers
cannot reach admin endpoints; sellers can only manage their own products.

## Folder Structure

```
.
├── backend/                        # Spring Boot REST API
│   └── src/
│       ├── main/java/com/sharveshmart/
│       │   ├── config/             # SecurityConfig, AppConfig, DemoDataInitializer
│       │   ├── controller/         # REST controllers
│       │   ├── dto/                # Request/response records (validation)
│       │   ├── entity/             # JPA entities + enums
│       │   ├── exception/          # Global exception handling (RFC 7807)
│       │   ├── repository/         # Spring Data JPA repositories
│       │   ├── security/           # JWT service, filter, UserPrincipal
│       │   └── service/            # Business logic + payment abstraction
│       └── src/main/resources/     # application.yml
│       └── src/test/               # 138 JUnit 5 tests
├── frontend/                       # React SPA
│   └── src/
│       ├── components/             # Reusable UI (Navbar, ProductCard, ui/)
│       ├── layouts/                # RootLayout
│       ├── pages/                  # Home, Login, Register, Products, ProductDetails, ...
│       ├── routes/                 # Route definitions & guards
│       ├── services/               # Axios client + API modules
│       ├── context/                # AuthContext (JWT state)
│       ├── hooks/                  # useAuth
│       ├── utils/                  # storage helpers
│       └── assets/                 # Static assets
├── docs/                           # Problem statement, architecture, ER, class, schema
└── .github/workflows/ci.yml        # CI: backend build/test, frontend build
```

## Backend Structure

- **Entities (11):** `User`, `Category`, `Product`, `ProductFile`, `Cart`, `CartItem`, `Order`,
  `OrderItem`, `Payment`, `Review`, `PurchaseEntitlement`
- **Enums (4):** `UserRole` (USER/SELLER/ADMIN), `ProductStatus`
  (DRAFT/PENDING_APPROVAL/APPROVED/REJECTED/ARCHIVED), `OrderStatus`
  (PENDING/PAID/FAILED/CANCELLED), `PaymentStatus` (PENDING/SUCCESS/FAILED)
- **Repositories (11):** Spring Data JPA interfaces with derived query methods
- **Services (7):** `UserService`, `CategoryService`, `ProductService`, `CartService`,
  `OrderService`, `PaymentService`, `PurchaseEntitlementService`, `ReviewService`
- **Controllers (11):** Auth, Home, Users, Categories, Products, Cart, Orders, Payments,
  Digital Library, Product Moderation, Reviews

## Frontend Structure

A clean React SPA organized into `components/`, `layouts/`, `pages/`, `routes/`, `services/`,
`hooks/`, `context/`, and `utils/`. Routing uses React Router v7; authentication state lives in
`AuthContext` and is persisted to `localStorage`. All API calls go through a central Axios client
that attaches the JWT and handles 401/403 responses.

## JWT Authentication

1. **Register** — `POST /api/auth/register`. Role is optional (`USER` by default); only `USER` and
   `SELLER` can self-register (the format `ADMIN` role is never accepted from a client).
2. **Login** — `POST /api/auth/login`. Credentials are verified against the BCrypt hash; on success
   the backend issues a signed JWT with a 24-hour expiry containing the user id and role.
3. **Every request** — the frontend sends `Authorization: Bearer <token>`. A
   `JwtAuthenticationFilter` validates the signature and expiry and populates the security context.
4. **Authorization** — `@PreAuthorize("hasRole('...')")` guards protected endpoints; missing or
   invalid tokens receive **401**, authenticated-but-forbidden requests receive **403**.
5. **Frontend** — the JWT is stored in `localStorage`, attached by an Axios interceptor; a 401
   response clears the session and redirects to login, a 403 redirects to the Forbidden page.

No refresh tokens, no OAuth, no server-side sessions.

## Purchase Entitlement Concept

`PurchaseEntitlement` is a first-class entity `(user, product, order, grantedAt)`. It is created
**only** when an order's payment succeeds:

1. Buyer checks out a cart → `PENDING` order (totals computed server-side).
2. Buyer pays → `MockPaymentProcessor` returns success → order becomes `PAID`, payment becomes
   `SUCCESS`.
3. `Paymentservice` calls `PurchaseEntitlementService.grantForOrder(order)` — one entitlement per
   purchased product (idempotent: `(user, product)` is unique).
4. Entitled products appear in the user's **Digital Library**.
5. `GET /api/library/products/{productId}/download` returns file metadata **only** if an
   entitlement exists — otherwise **403**.

Download authorization is therefore based on verified purchase ownership, never on guessing a file
URL.

## Environment Variables

All values have safe local-development defaults.

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | Backend server port |
| `DATABASE_URL` | `jdbc:postgresql://127.0.4.7:5432/sharveshmart` | JDBC URL (PostgreSQL) |
| `DATABASE_DRIVER` | `org.postgresql.Driver` | JDBC driver |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | *(empty — set to your PostgreSQL password)* | Database password |
| `JPA_DIALECT` | `org.hibernate.dialect.PostgreSQLDialect` | Hibernate dialect |
| `JWT_SECRET` | dev fallback (see note) | HS256 secret — **must be ≥ 32 bytes in production** |
| `JWT_EXPIRATION_MS` | `86400000` | Token lifetime (24 hours) |
| `app.demo-data.enabled` | `true` | Demo data seed (idempotent; set `false` in production) |
| `VITE_API_URL` | *(empty)* | Frontend API base URL (`/api` via dev proxy by default) |

> **Security note:** the `JWT_SECRET` fallback is for local development only. Set a real secret via
> the `JWT_SECRET` environment variable before any deployment.

## Backend Setup

Requirements: **Java 17+**, **Maven**, and a **PostgreSQL 15+** instance.

1. **Create the database** (the app does not auto-create it):
   ```sql
   CREATE DATABASE sharveshmart;
   ```
2. **Set the database password** (the app defaults `DB_USERNAME=postgres`, `DB_PASSWORD` empty):
   ```bash
   export DB_PASSWORD="your_postgres_password"        # PowerShell: $env:DB_PASSWORD="..."
   ```
   Or put `DB_USERNAME` / `DB_PASSWORD` in `backend/src/main/resources/application.yml`.
3. **Build & run:**
   ```bash
   cd backend
   ./mvnw.cmd test                 # run the 138 JUnit 5 tests
   ./mvnw.cmd spring-boot:run      # start the API on http://localhost:8080
   ```

The backend connects to PostgreSQL by default. Hibernate creates/updates the schema
(`ddl-auto: update`), and a development-only `DemoDataInitializer` seeds demo categories,
approved products, and demo users when `app.demo-data.enabled=true` (it is idempotent and
should be disabled in production via `app.demo-data.enabled=false`).

**Swagger UI:** http://localhost:8080/swagger-ui.html
**OpenAPI JSON:** http://localhost:8080/v3/api-docs

## Frontend Setup

Requirements: **Node.js 18+** (CI uses Node 26).

```bash
cd frontend
npm install
npm run dev                     # Vite dev server on http://localhost:5173
npm run build                   # production build
```

The dev server proxies `/api` → `http://localhost:8080`, so no CORS configuration is needed and no
`.env` file is required for local development. To point the frontend at a different backend, set
`VITE_API_URL` (see `frontend/.env.example`).

## Testing

- **Backend:** 138 tests via JUnit 5 — entity/repository structure contracts, service unit tests,
  `@WebMvcTest` controller slice tests (MockMvc + mocked services), JWT unit tests, and a full
  `@SpringBootTest` `ApiFlowIntegrationTest` covering the entire buyer journey
  (register → login → browse → cart → checkout → pay → library → download → review) plus 401/403
  negative cases.
- **Frontend:** `npm run build` produces a clean production bundle (no compile errors).

## API Overview

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create an account (USER/SELLER) |
| POST | `/api/auth/login` | Public | Log in, receive JWT |
| GET | `/` | Public | Backend banner |
| GET | `/api/users/{userId}` | Public | User profile |
| GET | `/api/categories` | Public | List categories |
| GET | `/api/products` | Public | Approved products (optional `categoryId`) |
| GET | `/api/products/{productId}` | Public | Approved product details |
| GET | `/api/sellers/{sellerId}/products` | Public | Seller listings |
| POST | `/api/products` | SELLER | Create product (DRAFT) |
| PUT | `/api/products/{productId}` | SELLER | Update own product |
| DELETE | `/api/products/{productId}` | SELLER | Archive own product |
| PATCH | `/api/products/{productId}/submit` | SELLER | Submit for approval |
| GET | `/api/products/{productId}/reviews` | Public | List reviews |
| POST | `/api/products/{productId}/reviews` | USER | Create review (buyers only) |
| PUT | `/api/reviews/{reviewId}` | USER | Update own review |
| DELETE | `/api/reviews/{reviewId}` | USER | Delete own review |
| GET | `/api/cart` | USER | View cart |
| POST | `/api/cart/items` | USER | Add item to cart |
| PUT | `/api/cart/items/{productId}` | USER | Update quantity |
| DELETE | `/api/cart/items/{productId}` | USER | Remove item |
| POST | `/api/orders/checkout` | USER | Checkout (creates order, clears cart) |
| GET | `/api/orders` | USER | Order history |
| GET | `/api/orders/{orderId}` | USER | Order detail |
| POST | `/api/orders/{orderId}/pay` | USER | Pay an order (mock provider) |
| GET | `/api/library` | USER | Digital Library (purchased products) |
| GET | `/api/library/products/{productId}/download` | USER | Download authorization |
| GET | `/api/admin/products/pending` | ADMIN | List products pending approval |
| POST | `/api/admin/products/{productId}/approve` | ADMIN | Approve a product |
| POST | `/api/admin/products/{productId}/reject` | ADMIN | Reject a product |

All protected endpoints return **401** without a valid JWT and **403** to the wrong role. Errors are
returned as RFC 7807 `ProblemDetail` JSON.

## Current Project Status

Phase 1 (backend foundation) is **complete, verified, and pushed**:

- ✅ JPA domain model, repositories, services, REST API, and JWT security implemented
- ✅ 138 backend tests passing; frontend builds cleanly
- ✅ Swagger UI documented
- ✅ Review-I MVP frontend (Home / Login / Register / Products / Product Details / 404 /
  Unauthorized / Forbidden) wired to the backend API
- ✅ Validation, exception handling, and role-based authorization in place

**Backend APIs for the full buying flow** (cart → checkout → payment → library → download) are
implemented and demonstrable through Swagger UI.

## Demo Credentials

Seeded automatically on PostgreSQL when `app.demo-data.enabled=true` (local development):

| Role | Email | Password |
|---|---|---|
| ADMIN | `admin@demo.com` | `DemoPass123!` |
| SELLER | `seller@demo.com` | `DemoPass123!` |
| USER | `buyer@demo.com` | `DemoPass123!` |

The demo seed also provides categories and several **approved** products (several with file
metadata) so the catalog, product details, and purchase/download flows can be demonstrated without
manual setup.

## Future Improvements

- Public catalog search, filtering, and sorting
- Category management (admin)
- Real digital file upload/storage behind the file abstraction
- Real payment provider integration behind the `PaymentProcessor` abstraction
- Seller and admin dashboards, order/transaction monitoring UI
- Pagination for large catalogs
- Docker images and deployment (POST VI) / production PostgreSQL configuration
- AI-assisted discovery and recommendations (post-MVP)