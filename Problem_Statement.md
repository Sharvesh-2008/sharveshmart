# Problem Statement

## 1. Title
**Digital Products Marketplace** — an online platform for buying and selling digital products, including ebooks, software, digital templates, design assets, and courses.

## 2. Domain
E-commerce / Digital Goods Marketplace

The project specifically concerns **digital** products, not physical products. Examples include:
- Ebooks
- Software
- Digital templates
- Design assets
- Courses
- Other downloadable digital resources

## 3. Who is the user? (2-3 user types, with roles)

### User / Buyer
- Register and log in
- Browse digital products
- Search, filter, and sort products
- View product details
- Manage cart
- Purchase products
- View purchase history
- Access purchased products
- Download products they are authorized to access
- Submit eligible reviews/ratings

### Seller
- Register and log in
- Create digital product listings
- Manage their own products
- Upload/manage digital product information
- View their products
- View relevant sales/order information
- Submit products for admin approval

### Admin
- Manage users
- Manage sellers
- Manage categories
- Review/approve/reject products
- Manage marketplace products
- Monitor orders/transactions
- Monitor marketplace activity
- Remove or moderate inappropriate content/users where required

**Role boundaries:** Sellers may only manage their own products. Users must not access seller functionality. Users and sellers must not access admin functionality.

## 4. What problem are we solving?
Many independent creators and sellers of digital products (ebooks, software, templates, design assets) have no simple, centralized way to sell their work with automated, controlled delivery. Manual payment and file-transfer processes are fragmented, forcing sellers to chase payments and buyers to rely on informal arrangements. Buyers struggle to discover trusted, verified digital products in one place, while sellers lack a structured product catalog and transaction process. Digital delivery also requires controlled access after a successful purchase, so buyers can only download what they actually own. A centralized marketplace addresses this by combining product discovery, transactions, purchase tracking, and digital access in a single workflow.

## 5. Proposed Solution
The application implements the CSE specialization: **"Design a digital marketplace with an optimized product catalog and transaction flow."**

**Catalog**
- Organized digital product catalog with categories
- Product detail pages
- Search, filtering, and sorting
- Product discovery with approval/status visibility

**Marketplace**
- User and seller registration/login
- Seller product management (create, edit, manage own listings)
- Product approval workflow
- Cart, checkout, and order creation
- Transaction/payment status tracking
- Purchase history

**Digital Access**
- Digital library of purchased products
- Purchase ownership / entitlement
- Controlled download and access
- Backend authorization before download

**Reviews**
- Product reviews and ratings from eligible buyers

**Admin**
- User and seller management
- Product moderation
- Category management
- Order/transaction monitoring

**Core transaction concept** — the product journey flows through: Product → Cart → Checkout → Transaction/Payment → Verification → Order → Purchase Ownership/Entitlement → Digital Library → Authorized Download.

A user must not be able to download a digital product merely by knowing or guessing a file URL. Access is granted only after the authenticated user's valid purchase ownership is verified, and this is described as **controlled access to purchased digital products after successful transaction verification** (not via license keys).

## 6. Core Entities / Database Tables
The following core entities represent the domain model (presented conceptually; final physical tables may be refined during detailed database design):

1. **User** — id, name, email, password, role, created_at
2. **Product** — id, seller_id, category_id, title, description, price, file, status, created_at
3. **Category** — id, name, description
4. **Order** — id, user_id, total_amount, status, created_at
5. **OrderItem** — id, order_id, product_id, unit_price, quantity
6. **Payment / Transaction** — id, order_id, amount, method, status, transaction_reference, paid_at
7. **Review** — id, product_id, user_id, rating, comment, created_at
8. **PurchaseEntitlement / DigitalAccess** — id, user_id, product_id, order_id, granted_at

Additional entities (e.g., SellerProfile, Cart, CartItem, Wishlist, ProductFile, DownloadLog) may be introduced later during detailed database design if justified; they are not required core entities at this stage.

## 7. User Roles & Permissions

### Admin
- Platform-level management
- User and seller management
- Product moderation
- Category management
- Order/transaction monitoring
- Marketplace oversight

### Seller
- Create/manage own products
- Upload/manage digital product information
- Submit products for approval
- View own product/sales information

### User
- Browse/search/filter/sort
- View product details
- Manage cart
- Purchase
- View purchase history
- Access purchased products
- Download authorized products
- Submit eligible reviews

Consistent role terminology is used throughout: **USER, SELLER, ADMIN**.

## 8. Success Criteria
- A user can register/login successfully and access authorized features.
- A user can discover products using catalog search/filter/sort functionality.
- A user can add an available product to a cart and complete the defined checkout/transaction flow.
- A successful transaction creates the appropriate order and purchase ownership.
- A user can access/download a purchased digital product only after ownership is verified.
- A seller can create and manage their own product listings.
- An admin can review and moderate products.
- Core data is persisted correctly in PostgreSQL.
- The core marketplace workflows operate correctly with appropriate validation and authorization in the deployed application.

## 9. Out of Scope
- Physical product delivery/shipping
- In-app buyer/seller chat
- Recurring subscriptions
- Cryptocurrency payments
- Complex refund/dispute-resolution workflows
- Native mobile applications
- Production-grade financial/payment infrastructure
- Microservices architecture
- Real-time communication systems
- Complex DRM/digital watermarking
- Large-scale enterprise infrastructure
- Advanced AI-powered recommendation or content-generation features are outside the initial MVP and may be considered as a future enhancement.

The initial MVP remains a well-scoped, monolithic full-stack marketplace with meaningful business logic — not a large-scale enterprise platform.

## 10. Chosen Track
**Track:** Java (Spring Boot)

**Frontend:** React.js + Tailwind CSS + Axios
**Backend:** Spring Boot 3.x + Java 17 + Maven
**Security:** Spring Security + JWT + JJWT
**Data Layer:** Spring Data JPA + Hibernate
**Database:** PostgreSQL 15
**Testing:** JUnit 5
**API Documentation:** springdoc-openapi + Swagger UI
**CI/CD:** GitHub Actions
**Containerization:** Docker — optional bonus / planned later

Deployment targets: Frontend on Vercel, Backend on Render, Managed PostgreSQL on Aiven.
