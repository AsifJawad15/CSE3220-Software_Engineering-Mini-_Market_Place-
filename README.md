# MiniMarketPlace

A full-stack multi-vendor marketplace built with **Spring Boot 3.5**, **Thymeleaf**, **PostgreSQL**, and **Docker**.  
Three roles — **Admin**, **Seller**, **Buyer** — with session-based security, product catalog, cart, checkout, and order management.

> **Course:** CSE 3220 — Software Engineering Lab  
> **Author:** Asif Jawad

---

## Table of Contents

- [Architecture](#architecture)
- [ER Diagram](#er-diagram)
- [Tech Stack](#tech-stack)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Running with Docker](#running-with-docker)
- [Running Locally (without Docker)](#running-locally-without-docker)
- [Testing](#testing)
- [CI/CD Pipeline](#cicd-pipeline)
- [Deployment (Render)](#deployment-render)
- [Seeded Admin](#seeded-admin)

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                           Client (Browser)                          │
└──────────────────────────┬───────────────────────────────────────────┘
                           │ HTTP (HTML + REST API)
┌──────────────────────────▼───────────────────────────────────────────┐
│                     Spring Boot Application                         │
│                                                                     │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────────────┐   │
│  │  Thymeleaf  │  │ REST API     │  │  Spring Security         │   │
│  │  Controllers│  │ Controllers  │  │  (Session + BCrypt)      │   │
│  └──────┬──────┘  └──────┬───────┘  └──────────────────────────┘   │
│         │                │                                          │
│  ┌──────▼────────────────▼──────────────────────────────────────┐   │
│  │                   Service Layer                              │   │
│  │  AuthService · ProductService · CartService · OrderService   │   │
│  │  CheckoutService · InventoryService · AdminService           │   │
│  └──────────────────────┬───────────────────────────────────────┘   │
│                         │                                           │
│  ┌──────────────────────▼───────────────────────────────────────┐   │
│  │              Spring Data JPA Repositories                    │   │
│  └──────────────────────┬───────────────────────────────────────┘   │
└──────────────────────────┼──────────────────────────────────────────┘
                           │ JDBC
┌──────────────────────────▼──────────────────────────────────────────┐
│                    PostgreSQL 16 (Docker)                           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## ER Diagram

```
┌─────────────┐      ┌──────────────────┐      ┌──────────────────┐
│    users     │      │  buyer_profiles  │      │    addresses     │
├─────────────┤      ├──────────────────┤      ├──────────────────┤
│ id (PK)     │◄──┐  │ id (PK)         │◄──┐  │ id (PK)         │
│ full_name   │   │  │ user_id (FK)    │   │  │ buyer_profile_id │
│ email (UQ)  │   │  │ phone           │   │  │ label           │
│ password    │   │  │ default_addr_id │   │  │ line1, line2    │
│ role        │   │  └──────────────────┘   │  │ city, postal    │
│ enabled     │   │                         │  │ country, phone  │
└─────────────┘   │                         │  └──────────────────┘
       │          │                         │
       │   ┌──────┴──────────┐              │
       └──►│ seller_profiles │    ┌─────────┴────────┐
           ├─────────────────┤    │     carts         │
           │ id (PK)        │    ├────────────────────┤
           │ user_id (FK)   │    │ id (PK)           │
           │ shop_name      │    │ buyer_profile_id  │
           │ phone          │    └────────┬───────────┘
           │ approval_status│             │
           └───────┬────────┘    ┌────────▼───────────┐
                   │             │    cart_items       │
           ┌───────▼────────┐   ├─────────────────────┤
           │   categories   │   │ id (PK)            │
           ├────────────────┤   │ cart_id (FK)       │
           │ id (PK)       │   │ product_id (FK)    │
           │ name (UQ)     │   │ quantity           │
           │ slug (UQ)     │   │ unit_price_snapshot│
           └───────┬────────┘   └─────────────────────┘
                   │
           ┌───────▼────────┐   ┌─────────────────────┐
           │   products     │   │      orders         │
           ├────────────────┤   ├─────────────────────┤
           │ id (PK)       │   │ id (PK)            │
           │ name          │   │ buyer_profile_id   │
           │ description   │   │ status             │
           │ price         │   │ total_amount       │
           │ stock_quantity│   │ shipping_address   │
           │ image_url     │   └────────┬────────────┘
           │ active        │            │
           │ category_id   │   ┌────────▼────────────┐
           │ seller_id     │   │    order_items      │
           └────────────────┘   ├─────────────────────┤
                                │ id (PK)            │
                                │ order_id (FK)      │
                                │ product_id (FK)    │
                                │ seller_id (FK)     │
                                │ quantity           │
                                │ price_at_purchase  │
                                └─────────────────────┘
```

---

## Tech Stack

| Layer       | Technology                                      |
|-------------|------------------------------------------------|
| Language    | Java 17                                        |
| Framework   | Spring Boot 3.5.11                             |
| Security    | Spring Security (session-based, BCrypt)         |
| Templates   | Thymeleaf + Bootstrap 5 + Bootstrap Icons       |
| ORM         | Spring Data JPA / Hibernate                    |
| Database    | PostgreSQL 16                                  |
| Build       | Maven (with wrapper)                           |
| Testing     | JUnit 5, Mockito, MockMvc, H2 (in-memory)     |
| Container   | Docker (multi-stage build)                     |
| CI/CD       | GitHub Actions                                 |
| Deployment  | Render (Web Service + PostgreSQL)              |

---

## API Endpoints

### Public (No Auth Required)

| Method | URL                                  | Description                   |
|--------|--------------------------------------|-------------------------------|
| GET    | `/`                                  | Redirect to `/products`       |
| GET    | `/login`                             | Login page                    |
| GET    | `/register/buyer`                    | Buyer registration form       |
| POST   | `/register/buyer`                    | Register buyer                |
| GET    | `/register/seller`                   | Seller registration form      |
| POST   | `/register/seller`                   | Register seller               |
| GET    | `/products`                          | Product catalog (paged)       |
| GET    | `/products/{id}`                     | Product detail page           |

### Public REST API

| Method | URL                                  | Description                   |
|--------|--------------------------------------|-------------------------------|
| GET    | `/api/categories`                    | List all categories           |
| GET    | `/api/categories/{id}`               | Get category by ID            |
| GET    | `/api/products`                      | List products (paged, search) |
| GET    | `/api/products/{id}`                 | Get product by ID             |

### Buyer (ROLE_BUYER)

| Method | URL                                  | Description                   |
|--------|--------------------------------------|-------------------------------|
| GET    | `/buyer/dashboard`                   | Buyer dashboard               |
| GET    | `/buyer/profile`                     | View profile                  |
| POST   | `/buyer/profile`                     | Update profile                |
| GET    | `/buyer/addresses`                   | Manage addresses              |
| POST   | `/buyer/addresses`                   | Add address                   |
| GET    | `/buyer/cart`                        | View cart                     |
| POST   | `/buyer/cart/add`                    | Add item to cart              |
| POST   | `/buyer/cart/update/{itemId}`        | Update cart item quantity      |
| POST   | `/buyer/cart/remove/{itemId}`        | Remove item from cart         |
| GET    | `/buyer/checkout`                    | Checkout page                 |
| POST   | `/buyer/checkout`                    | Place order                   |
| GET    | `/buyer/orders`                      | Order history                 |
| GET    | `/buyer/orders/{id}`                 | Order detail                  |
| POST   | `/buyer/orders/{id}/cancel`          | Cancel order                  |

### Buyer REST API

| Method | URL                                  | Description                   |
|--------|--------------------------------------|-------------------------------|
| GET    | `/api/cart`                          | Get cart (JSON)               |
| POST   | `/api/cart/items`                    | Add item to cart              |
| PUT    | `/api/cart/items/{itemId}`           | Update item quantity          |
| DELETE | `/api/cart/items/{itemId}`           | Remove item                   |
| POST   | `/api/orders/checkout`               | Checkout (JSON)               |
| GET    | `/api/orders`                        | Get buyer orders              |
| GET    | `/api/orders/{id}`                   | Get order detail              |
| POST   | `/api/orders/{id}/cancel`            | Cancel order                  |

### Seller (ROLE_SELLER)

| Method | URL                                  | Description                   |
|--------|--------------------------------------|-------------------------------|
| GET    | `/seller/dashboard`                  | Seller dashboard              |
| GET    | `/seller/profile`                    | View profile                  |
| POST   | `/seller/profile`                    | Update profile                |
| GET    | `/seller/products`                   | List seller's products        |
| GET    | `/seller/products/new`               | New product form              |
| POST   | `/seller/products/new`               | Create product                |
| GET    | `/seller/products/{id}/edit`         | Edit product form             |
| POST   | `/seller/products/{id}/edit`         | Update product                |
| POST   | `/seller/products/{id}/delete`       | Delete product                |
| GET    | `/seller/orders`                     | View seller's order items     |
| POST   | `/seller/orders/{orderId}/advance`   | Advance order status          |

### Admin (ROLE_ADMIN)

| Method | URL                                  | Description                   |
|--------|--------------------------------------|-------------------------------|
| GET    | `/admin/dashboard`                   | Admin dashboard + stats       |
| GET    | `/admin/sellers`                     | Manage sellers                |
| POST   | `/admin/sellers/{id}/approve`        | Approve seller                |
| POST   | `/admin/sellers/{id}/reject`         | Reject seller                 |
| GET    | `/admin/users`                       | List all users                |
| GET    | `/admin/products`                    | Moderate products             |
| POST   | `/admin/products/{id}/toggle`        | Activate/deactivate product   |
| GET    | `/admin/orders`                      | View all orders               |

### Admin REST API

| Method | URL                                  | Description                   |
|--------|--------------------------------------|-------------------------------|
| GET    | `/api/admin/stats`                   | Dashboard statistics          |
| GET    | `/api/admin/users`                   | List all users                |
| GET    | `/api/admin/sellers`                 | List all sellers              |
| POST   | `/api/admin/sellers/{id}/approve`    | Approve seller                |
| POST   | `/api/admin/sellers/{id}/reject`     | Reject seller                 |
| GET    | `/api/admin/products`                | List all products             |
| POST   | `/api/admin/products/{id}/toggle`    | Toggle product active status  |
| GET    | `/api/admin/orders`                  | List all orders               |

---

## Getting Started

### Prerequisites

- **Java 17** (JDK)
- **Docker** & **Docker Compose**
- **Git**

### Clone the Repository

```bash
git clone https://github.com/AsifJawad15/CSE3220-Software_Engineering-Mini-_Market_Place-.git
cd CSE3220-Software_Engineering-Mini-_Market_Place-
```

---

## Running with Docker

The easiest way to run the entire application (app + database):

```bash
docker compose up --build
```

This will:
1. Start a **PostgreSQL 16** container with health checks
2. Build the Spring Boot app using a multi-stage Dockerfile
3. Start the app on **http://localhost:8080**

To stop:
```bash
docker compose down
```

To stop and remove data volumes:
```bash
docker compose down -v
```

---

## Running Locally (without Docker)

**Step 1:** Start only the PostgreSQL database:
```bash
docker compose up postgres -d
```

**Step 2:** Run the Spring Boot application:
```bash
./mvnw spring-boot:run
```

Or on Windows:
```powershell
.\mvnw.cmd spring-boot:run
```

The app will be available at **http://localhost:8080**.

---

## Testing

The project uses **JUnit 5 + Mockito** for unit tests and **@SpringBootTest + MockMvc** for integration tests with an **H2 in-memory database**.

### Run all tests:
```bash
./mvnw test
```

### Test Summary (55 tests):

| Test Class                            | Type        | Tests |
|---------------------------------------|-------------|-------|
| `AuthServiceTest`                     | Unit        | 6     |
| `ProductServiceTest`                  | Unit        | 8     |
| `CartServiceTest`                     | Unit        | 7     |
| `CheckoutServiceTest`                 | Unit        | 3     |
| `OrderServiceTest`                    | Unit        | 8     |
| `InventoryServiceTest`               | Unit        | 5     |
| `AdminServiceTest`                    | Unit        | 5     |
| `AuthControllerIntegrationTest`       | Integration | 5     |
| `ProductControllerIntegrationTest`    | Integration | 4     |
| `OrderControllerIntegrationTest`      | Integration | 4     |
| `MiniMarketPlaceApplicationTests`     | Integration | 1     |

---

## CI/CD Pipeline

The project uses **GitHub Actions** with the following pipeline:

```
Push / PR to main or develop
         │
         ▼
┌─────────────────┐
│   1. Checkout    │
│   2. Setup JDK  │
│   3. mvn test   │
│   4. mvn package│
└────────┬────────┘
         │ (only on push to main)
         ▼
┌─────────────────┐
│ 5. Build Docker │
│    Image        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 6. Deploy to    │
│    Render       │
└─────────────────┘
```

### Workflow File: `.github/workflows/ci-cd.yml`

- **On every push/PR:** Checkout → Setup Java 17 → Run tests → Build JAR
- **On push to `main` only:** Build Docker image → Deploy to Render via deploy hook

---

## Deployment (Render)

The application is deployed on **Render** as a Web Service:

1. **Render PostgreSQL** — managed database providing `DB_URL`, `DB_USER`, `DB_PASS`
2. **Render Web Service** — connected to GitHub repo, auto-deploys from `main`

### Environment Variables on Render:

| Variable   | Description                              |
|------------|------------------------------------------|
| `DB_URL`   | JDBC URL for Render PostgreSQL           |
| `DB_USER`  | Database username                        |
| `DB_PASS`  | Database password                        |
| `PORT`     | Port (Render sets this automatically)    |

### GitHub Secrets Required:

| Secret                  | Description                          |
|-------------------------|--------------------------------------|
| `RENDER_DEPLOY_HOOK_URL`| Render deploy hook URL for auto-deploy|

---

## Seeded Admin

On every startup, the application seeds an admin account (idempotent):

| Field    | Value              |
|----------|--------------------|
| Email    | `admin@market.com` |
| Password | `12345678`         |
| Role     | `ADMIN`            |

---

## Git Branch Strategy

```
main (protected — PR required)
└── develop (integration branch)
    ├── feature/project-skeleton    ✅
    ├── feature/auth-security       ✅
    ├── feature/profiles-address    ✅  (Phase 3)
    ├── feature/product-catalog     ✅  (Phase 4)
    ├── feature/cart-checkout       ✅  (Phase 5)
    ├── feature/rest-api            ✅  (Phase 6)
    ├── feature/testing             ✅  (Phase 7)
    └── feature/docker-cicd         ✅  (Phase 8)
```

---

## License

This project is part of the CSE 3220 Software Engineering Lab coursework.

