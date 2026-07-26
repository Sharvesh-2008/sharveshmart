# Problem Statement

## 1. Title
**Digital Market Place** — an online platform for buying and selling digital goods, including ebooks, software, and digital templates.

## 2. Domain
E-commerce / Digital Goods Marketplace

## 3. Who is the user? (2-3 user types, with roles)
- **Buyer (User):** Browses the catalog, searches for digital products, purchases items, and downloads licensed files after a successful payment.
- **Seller / Vendor (Vendor):** Registers as a merchant, creates and manages product listings, uploads digital files, tracks sales and earnings.
- **Administrator (Admin):** Moderates the platform, verifies sellers and products, monitors orders/payments, and handles reported content or users.

## 4. What problem are we solving?
Many creators and independent sellers of digital content (ebooks, software, templates, design assets) have no simple, centralized way to sell their products with automated, secure delivery. Historically sellers resort to manual payments and file transfers, which are time-consuming, unreliable, and prone to piracy; buyers struggle to find trusted, verified digital goods in one place. Transactions and downloads are fragmented across e-mail, messengers, and third-party payment links, leading to a poor experience on both sides. A web-based marketplace consolidates discovery, purchasing, payment, and instant license/file delivery in one workflow, providing a secure, measurable, and repeatable sales process for digital goods.

## 5. Proposed Solution (what the application will do, feature-wise)
- User registration, login, and profile management.
- Seller dashboard to create, update, and delete product listings (title, description, price, category, cover image, and uploaded digital file).
- Public catalog with search, filtering by category, and product detail pages.
- Shopping cart and checkout flow with order summary.
- Payment processing with payment status tracking.
- Instant delivery of purchased digital files and/or license keys after successful payment.
- Order history for both buyer and seller.
- Product reviews and ratings from buyers.
- Admin panel for user management, product moderation, and order/payment oversight.

## 6. Core Entities / Database Tables (list all, minimum 5)
1. **User** — id, name, email, password_hash, role, created_at
2. **Product** — id, seller_id, category_id, title, description, price, file_url, status, created_at
3. **Category** — id, name, description
4. **Order** — id, buyer_id, total_amount, status, created_at
5. **OrderItem** — id, order_id, product_id, unit_price, quantity
6. **Payment** — id, order_id, amount, method, transaction_id, status, paid_at
7. **Review** — id, product_id, buyer_id, rating, comment, created_at
8. **DownloadLog** — id, order_item_id, buyer_id, downloaded_at, download_count

## 7. User Roles & Permissions (minimum 2 distinct roles)
- **Admin**: Full access — manage users and sellers, moderate products, view all orders/payments, remove content.
- **Vendor (Seller)**: Create/manage own product listings, upload files, view own orders and earnings.
- **User (Buyer)**: Browse/search products, purchase, download purchased files, write reviews.

## 8. Success Criteria
- A buyer can search, purchase, and download a digital product in under 2 minutes.
- A seller can list a new product in under 3 minutes.
- A payment-to-delivery flow completes automatically without manual admin involvement.
- An admin can take down a reported product in under 10 minutes.
- The platform supports a minimum of 1,000 concurrent page views without service failure.

## 9. Out of Scope (clearly list what you will NOT build, to avoid over-commitment)
- Physical product delivery or tangible order shipping.
- In-app messaging or chat between buyers and sellers.
- Recurring billing / subscription plans.
- Crypto-currency payment support.
- Refund and dispute-resolution workflows.
- Mobile device applications.
- Marketing, SEO, or analytics dashboards.
- Digital watermarking or DRM protection of digital files.

## 10. Chosen Track: Java (Spring Boot)
- **Track**: Java (Spring Boot)