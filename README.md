# Digital Products Marketplace

An online platform for buying and selling digital products — ebooks, software, digital templates,
design assets, and courses. The system combines a structured product catalog with a controlled
transaction flow so that buyers can discover, purchase, and download digital products, while
sellers manage their own listings under admin moderation.

## Project Structure

```
backend/    Spring Boot REST API (Java 17, Maven)
frontend/   React.js single-page application (Vite, Tailwind CSS, Axios)
docs/       Project documentation (diagrams, database design)
.github/    GitHub Actions workflows
```

## Technology Stack

- **Frontend:** React.js, Tailwind CSS, Axios
- **Backend:** Spring Boot 3.x, Java 17, Maven
- **Security:** Spring Security, JWT, JJWT
- **Persistence:** Spring Data JPA, Hibernate, PostgreSQL 15
- **Testing:** JUnit 5
- **API Documentation:** springdoc-openapi, Swagger UI
- **CI/CD:** GitHub Actions
- **Containerization:** Docker (optional bonus, planned later)

## Current Status

The project is currently at the **scaffolding / setup stage**. The full-stack project skeleton
has been created and verified to build and run, but **no marketplace functionality has been
implemented yet**. Authentication, users, products, catalog, cart, orders, payments,
entitlements, downloads, reviews, and admin/seller features are **not yet built**.

## Documentation

- [Problem Statement](Problem_Statement.md)
- [System Architecture](docs/diagrams/system-architecture.md)
- [ER Diagram](docs/diagrams/er-diagram.md)
- [Class Diagram](docs/diagrams/class-diagram.md)
- [Database Schema](docs/database/schema.md)
