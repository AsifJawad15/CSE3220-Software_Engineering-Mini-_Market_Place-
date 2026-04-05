# MiniMarketPlace

> **CSE3220 — Software Engineering and Project Management Laboratory**
> Khulna University of Engineering & Technology

A full-stack multi-role marketplace web application built with **Spring Boot 3**, **Spring Security**, **Thymeleaf**, and **PostgreSQL**. The system supports three distinct roles — **Admin**, **Seller**, and **Buyer** — with complete authentication, product management, cart & checkout, and order tracking.

**Live URL:** [https://minimarketplace.onrender.com](https://minimarketplace.onrender.com)
**GitHub:** [https://github.com/AsifJawad15/CSE3220-Software_Engineering-Mini-_Market_Place-](https://github.com/AsifJawad15/CSE3220-Software_Engineering-Mini-_Market_Place-)

---

## Team Members

| Name | Roll |
|------|------|
| Asif Jawad | 2107007 |
| Salek Bin Hossain | 2107026 |

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Entity Relationship (ER) Diagram](#entity-relationship-er-diagram)
- [API Endpoints](#api-endpoints)
- [Security & Roles](#security--roles)
- [Run Locally with Docker](#run-locally-with-docker)
- [CI/CD Pipeline](#cicd-pipeline)
- [Testing](#testing-summary)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.11 |
| Security | Spring Security (BCrypt, session-based form login) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL 16 |
| Templates | Thymeleaf + Bootstrap 5 |
| Build | Maven |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions → Render |

---

## Architecture

The application follows a strict **Layered Architecture**:

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  Thymeleaf Templates  │  REST Controllers (@RestController)│
├─────────────────────────────────────────────────────────┤
│                   Controller Layer                       │
│  AuthController │ ProductController │ BuyerController   │
│  SellerController │ AdminController │ OrderController   │
│  CartController  │ REST equivalents                     │
├─────────────────────────────────────────────────────────┤
│                   Service Layer                          │
│  AuthService │ ProductService │ CategoryService         │
│  BuyerProfileService │ SellerProfileService             │
│  CartService │ CheckoutService │ OrderService           │
│  InventoryService                                        │
├─────────────────────────────────────────────────────────┤
│                 Repository Layer (JPA)                   │
│  UserRepository │ ProductRepository │ CategoryRepository│
│  BuyerProfileRepository │ AddressRepository             │
│  SellerProfileRepository │ CartRepository               │
│  OrderRepository │ OrderItemRepository                  │
├─────────────────────────────────────────────────────────┤
│               Database: PostgreSQL                       │
└─────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.asif.minimarketplace
├── common/          # BaseEntity, exceptions, GlobalExceptionHandler, ApiResponse DTO
├── config/          # SecurityConfig, JpaAuditingConfig, DataInitializer, HomeController
├── security/        # CustomUserDetailsService
├── user/            # User entity, RoleName enum, UserRepository
├── auth/            # Registration & login (controller, service, DTOs)
├── buyer/           # BuyerProfile, Address (entity, repository, service, controller)
├── seller/          # SellerProfile, ApprovalStatus (entity, repository, service, controller)
├── product/         # Product, Category (entity, repository, service, controller, REST)
├── cart/            # Cart, CartItem (entity, repository, service, controller, REST)
└── order/           # Order, OrderItem, OrderStatus (entity, repository, service, controller, REST)
```

---

## Entity Relationship (ER) Diagram

```
USER ──────────────── BUYER_PROFILE ────────────── ADDRESS
 │  1:1                      │ 1:M
 │                           │
 │  1:1                   CART ──── CART_ITEM ──── PRODUCT
 │                                                    │  M:1
SELLER_PROFILE                                     CATEGORY
 │  1:M
 │
PRODUCT ────────────── ORDER_ITEM ──────────────── ORDER
                                                     │ M:1
                                               BUYER_PROFILE

Tables: user, buyer_profile, address, seller_profile,
        product, category, cart, cart_item, order, order_item
```

| Entity | Key Fields |
|--------|-----------|
| `user` | id, full_name, email, password (BCrypt), role, enabled |
| `buyer_profile` | id, user_id (FK), phone, default_address_id |
| `address` | id, buyer_profile_id (FK), label, line1, city, postal, country |
| `seller_profile` | id, user_id (FK), shop_name, phone, approval_status |
| `category` | id, name, slug (unique) |
| `product` | id, name, description, price, stock_quantity, image_url, active, category_id (FK), seller_id (FK) |
| `cart` | id, buyer_profile_id (FK) |
| `cart_item` | id, cart_id (FK), product_id (FK), quantity, unit_price_snapshot |
| `order` | id, buyer_profile_id (FK), status, total_amount, shipping_address |
| `order_item` | id, order_id (FK), product_id (FK), seller_profile_id (FK), quantity, price_at_purchase |

---

## API Endpoints

### Public Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/products` | Browse all active products (search + filter) |
| GET | `/products/{id}` | Product detail page |
| GET | `/api/products` | REST: paginated active products |
| GET | `/api/products/{id}` | REST: single product |
| GET | `/api/categories` | REST: all categories |
| GET | `/register/buyer` | Buyer registration page |
| POST | `/register/buyer` | Register as buyer |
| GET | `/register/seller` | Seller registration page |
| POST | `/register/seller` | Register as seller |
| GET | `/login` | Login page |

### Buyer Endpoints (ROLE_BUYER)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/buyer/dashboard` | Buyer dashboard |
| GET/POST | `/buyer/profile` | View/update buyer profile |
| GET/POST | `/buyer/addresses` | List/add addresses |
| POST | `/buyer/addresses/{id}/default` | Set default address |
| POST | `/buyer/addresses/{id}/delete` | Delete address |
| GET | `/buyer/cart` | View cart |
| POST | `/buyer/cart/add` | Add item to cart |
| POST | `/buyer/cart/update/{itemId}` | Update item quantity |
| POST | `/buyer/cart/remove/{itemId}` | Remove cart item |
| GET | `/buyer/checkout` | Checkout page |
| POST | `/buyer/checkout` | Place order |
| GET | `/buyer/orders` | Order history |
| GET | `/buyer/orders/{id}` | Order detail |
| POST | `/buyer/orders/{id}/cancel` | Cancel order |
| GET | `/api/cart` | REST: get cart |
| POST | `/api/cart/items` | REST: add to cart |
| PATCH | `/api/cart/items/{id}` | REST: update quantity |
| DELETE | `/api/cart/items/{id}` | REST: remove cart item |
| DELETE | `/api/cart` | REST: clear cart |
| POST | `/api/buyer/orders/checkout` | REST: place order |
| GET | `/api/buyer/orders` | REST: buyer order list |
| GET | `/api/buyer/orders/{id}` | REST: order detail |

### Seller Endpoints (ROLE_SELLER)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/seller/dashboard` | Seller dashboard |
| GET/POST | `/seller/profile` | View/update seller profile |
| GET | `/seller/products` | Seller's product list |
| GET | `/seller/products/new` | New product form |
| POST | `/seller/products` | Create product |
| GET | `/seller/products/{id}/edit` | Edit product form |
| POST | `/seller/products/{id}` | Update product |
| POST | `/seller/products/{id}/delete` | Delete product |
| GET | `/seller/orders` | Seller's order items |
| POST | `/seller/orders/{id}/advance` | Advance order status |
| GET | `/api/seller/orders` | REST: seller order items |

### Admin Endpoints (ROLE_ADMIN)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/admin/dashboard` | Admin dashboard with stats |
| GET | `/admin/sellers` | Pending/all seller list |
| POST | `/admin/sellers/{id}/approve` | Approve seller |
| POST | `/admin/sellers/{id}/reject` | Reject seller |
| GET | `/admin/users` | All users |
| GET | `/admin/products` | All products |
| POST | `/admin/products/{id}/toggle` | Activate/deactivate product |
| GET | `/admin/orders` | All orders |
| GET | `/api/admin/stats` | REST: dashboard stats |
| GET | `/api/admin/users` | REST: user list |
| GET | `/api/admin/sellers/pending` | REST: pending sellers |

---

## Security & Roles

Spring Security is configured with **URL-based access rules** and **BCrypt** password encryption.

| Role | Registration | Access |
|------|-------------|--------|
| **ADMIN** | Seeded at startup (`admin@market.com` / `12345678`) | Full system access |
| **SELLER** | `/register/seller` | Product management, order fulfillment (requires admin approval) |
| **BUYER** | `/register/buyer` | Browse, cart, checkout, order history |

**Login redirect by role:**
- `ADMIN` → `/admin/dashboard`
- `SELLER` → `/seller/dashboard`
- `BUYER` → `/buyer/dashboard`

**Global Exception Handler** returns:
- JSON responses for `/api/**` endpoints
- Thymeleaf error views (`400`, `403`, `404`, `500`, `stock`) for web requests

---

## Run Locally with Docker

**Prerequisites:** Docker Desktop installed and running.

```bash
# Clone the repository
git clone https://github.com/AsifJawad15/CSE3220-Software_Engineering-Mini-_Market_Place-.git
cd CSE3220-Software_Engineering-Mini-_Market_Place-

# Start the app and PostgreSQL
docker compose up --build
```

The app will be available at **http://localhost:8080**.

Default admin credentials:
- Email: `admin@market.com`
- Password: `12345678`

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://db:5432/minimarketplace` |
| `DB_USER` | Database username | `miniuser` |
| `DB_PASS` | Database password | `minipass` |
| `PORT` | Application server port | `8080` |

> No credentials are hardcoded. All sensitive values are injected via environment variables.

### Run Tests

```bash
./mvnw test
```

Expected: `Tests run: 163, Failures: 0, Errors: 0 — BUILD SUCCESS`

---

## CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/ci-cd.yml`) runs on every push and PR to `main` and `develop`.

```
Push to main/develop
        │
        ▼
┌──────────────────┐
│  Build & Test    │  → mvnw test (H2 in-memory DB)
│  (ubuntu-latest) │  → mvnw package -DskipTests
└────────┬─────────┘
         │ (main branch only)
         ▼
┌──────────────────┐
│  Build Docker    │  → docker build -t minimarketplace:latest .
│  Image           │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Deploy to Render│  → HTTP POST to RENDER_DEPLOY_HOOK_URL secret
└──────────────────┘
```

**Branch Protection:**
- `main` is protected — direct push is disabled
- All changes require a Pull Request with at least 1 review approval
- CI must pass before merge

**Git Branch Strategy:**
```
main (protected)
└── develop (integration)
    ├── feature/project-skeleton   ✅ merged
    ├── feature/auth-security      ✅ merged
    ├── feature/profiles-address   ✅ merged
    ├── feature/product-catalog    ✅ merged
    ├── feature/cart-checkout      ✅ merged
    ├── feature/rest-api           ✅ merged
    ├── feature/testing            ✅ merged
    └── feature/docker-cicd        ✅ merged
```

---

## Testing Summary

The project includes **163 tests** covering unit tests, integration tests, security authorization tests, and exception handling tests. All tests pass with **0 failures**.

```
Tests run: 163, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

### How to Run Tests

```bash
./mvnw test
```

---

## Test Breakdown by Module

### 1. Auth Controller Tests — `AuthControllerTest` (9 tests)

Integration tests for login and registration endpoints using MockMvc standalone setup.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `loginPage_Loads` | GET `/login` returns 200 and renders login view |
| 2 | `buyerRegisterPage_Loads` | GET `/register/buyer` returns 200 and renders buyer registration form |
| 3 | `sellerRegisterPage_Loads` | GET `/register/seller` returns 200 and renders seller registration form |
| 4 | `validBuyerRegistration_RedirectsToLogin` | POST `/register/buyer` with valid data redirects to `/login` |
| 5 | `validSellerRegistration_RedirectsToLogin` | POST `/register/seller` with valid data redirects to `/login` |
| 6 | `duplicateBuyerEmail_ShowsErrorOnForm` | Duplicate email during buyer registration returns form with error |
| 7 | `duplicateSellerEmail_ShowsErrorOnForm` | Duplicate email during seller registration returns form with error |
| 8 | `invalidBuyerForm_ReturnsSamePage` | Invalid buyer registration (blank fields) stays on same page |
| 9 | `invalidSellerForm_ReturnsSamePage` | Invalid seller registration (blank fields) stays on same page |

### 2. Auth Service Tests — `AuthServiceTest` (7 tests)

Unit tests for `AuthService` business logic with Mockito mocks.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `registerBuyer_Success` | Buyer registration creates user with BUYER role and buyer profile |
| 2 | `registerBuyer_EmailIsNormalized` | Email is converted to lowercase before saving |
| 3 | `registerBuyer_EmailExists` | Throws exception when email already exists |
| 4 | `registerBuyer_PasswordsDoNotMatch` | Throws exception when password and confirmPassword don't match |
| 5 | `registerSeller_Success` | Seller registration creates user with SELLER role and seller profile |
| 6 | `registerSeller_EmailExists` | Throws exception when email already exists |
| 7 | `registerSeller_PasswordsDoNotMatch` | Throws exception when passwords don't match |

### 3. Product Controller Tests — `ProductControllerTest` (6 tests)

Integration tests for public product browsing pages.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `listProducts_PublicProductListLoads` | GET `/products` returns 200 and renders product list |
| 2 | `listProducts_SearchQueryFiltersResults` | GET `/products?q=keyword` passes search query to service |
| 3 | `listProducts_CategoryFilterWorks` | GET `/products?categoryId=1` filters products by category |
| 4 | `productDetail_ValidProduct_Loads` | GET `/products/1` loads product detail page |
| 5 | `productDetail_MissingProduct_ReturnsErrorPage` | GET `/products/999` for nonexistent product returns 404 error page |
| 6 | `productDetail_InactiveProduct_NotShown` | Inactive product returns 404 error page |

### 4. REST Product Controller Tests — `RestProductControllerTest` (3 tests)

Integration tests for public REST product API endpoints.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `listProducts_Returns200` | GET `/api/products` returns 200 with success JSON |
| 2 | `getProduct_BadId_Returns404` | GET `/api/products/999` returns 404 JSON for missing product |
| 3 | `listCategories_Returns200` | GET `/api/categories` returns 200 with success JSON |

### 5. Buyer Controller Tests — `BuyerControllerTest` (9 tests)

Integration tests for buyer dashboard, profile, and address management.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `dashboard_LoadsForBuyer` | GET `/buyer/dashboard` renders buyer dashboard |
| 2 | `profile_LoadsProfilePage` | GET `/buyer/profile` renders profile page with user data |
| 3 | `updateProfile_ValidRequest_Redirects` | POST `/buyer/profile` with valid data redirects with success |
| 4 | `updateProfile_InvalidRequest_StaysOnPage` | Invalid profile update stays on profile page with errors |
| 5 | `addresses_LoadsPage` | GET `/buyer/addresses` loads addresses page |
| 6 | `addAddress_ValidRequest_Redirects` | POST `/buyer/addresses` with valid address redirects |
| 7 | `addAddress_InvalidRequest_StaysOnPage` | Invalid address submission stays on page |
| 8 | `setDefaultAddress_Works` | POST `/buyer/addresses/1/default` sets default and redirects |
| 9 | `deleteAddress_Works` | POST `/buyer/addresses/1/delete` deletes and redirects |

### 6. Buyer Profile Service Tests — `BuyerProfileServiceTest` (13 tests)

Unit tests for `BuyerProfileService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `createProfile_Success` | Creating a buyer profile saves correctly |
| 2 | `getProfileByUserId_Success` | Retrieves profile by user ID |
| 3 | `getProfileByUserId_NotFound` | Throws NotFoundException for missing profile |
| 4 | `updateProfile_Success` | Updating profile fields works |
| 5 | `getAddresses_Success` | Returns list of addresses for a buyer |
| 6 | `addAddress_FirstAddressBecomesDefault` | First address is automatically set as default |
| 7 | `addAddress_MakeDefaultClearsOldDefault` | Setting new default clears previous default |
| 8 | `updateAddress_Success` | Updating an address works |
| 9 | `updateAddress_AccessDenied` | Cannot update another buyer's address |
| 10 | `deleteAddress_Success` | Deleting own address works |
| 11 | `deleteAddress_AccessDenied` | Cannot delete another buyer's address |
| 12 | `setDefaultAddress_Success` | Setting default address works |
| 13 | `setDefaultAddress_AccessDenied` | Cannot set default on another buyer's address |

### 7. Cart Controller Tests — `CartControllerTest` (2 tests)

Integration tests for cart web pages.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `viewCart_LoadsPage` | GET `/buyer/cart` renders cart page |
| 2 | `addToCart_ValidRequest_Redirects` | POST `/buyer/cart/add` adds item and redirects |

### 8. REST Cart Controller Tests — `RestCartControllerTest` (6 tests)

Integration tests for cart REST API endpoints.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `getCart_Returns200` | GET `/api/cart` returns 200 with cart JSON |
| 2 | `addItem_Returns201` | POST `/api/cart/items` returns 201 Created |
| 3 | `updateItem_QuantityGreaterThanZero_Returns200` | PATCH `/api/cart/items/1` with quantity > 0 returns 200 |
| 4 | `updateItem_QuantityZeroOrLess_ReturnsNoContent` | PATCH `/api/cart/items/1` with quantity ≤ 0 returns 204 (removes item) |
| 5 | `removeItem_Returns204` | DELETE `/api/cart/items/1` returns 204 |
| 6 | `clearCart_Returns204` | DELETE `/api/cart` returns 204 |

### 9. Cart Service Tests — `CartServiceTest` (13 tests)

Unit tests for `CartService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `getOrCreateCart_ReturnsExistingCart` | Returns existing cart for user |
| 2 | `getOrCreateCart_CreatesMissingCart` | Creates new cart if none exists |
| 3 | `addItem_AddNewItemWorks` | Adding a new product to cart works |
| 4 | `addItem_AddingSameProductIncreasesQuantity` | Adding same product increases quantity |
| 5 | `addItem_ThrowsIfProductInactive` | Throws exception for inactive product |
| 6 | `addItem_ThrowsIfOutOfStock` | Throws exception for out-of-stock product |
| 7 | `updateItemQuantity_ValidNumberWorks` | Updating quantity to valid number works |
| 8 | `updateItemQuantity_AboveStockFails` | Updating quantity above stock throws exception |
| 9 | `updateItemQuantity_ZeroRemovesItem` | Setting quantity to 0 removes item |
| 10 | `updateItemQuantity_MissingItemThrowsNotFound` | Updating non-existent item throws NotFoundException |
| 11 | `removeItem_SpecificItemWorks` | Removing a specific item works |
| 12 | `clearCart_RemovesAllItems` | Clearing cart removes all items |
| 13 | `calculateTotal_CorrectWithSnapshot` | Total calculation uses price snapshot correctly |

### 10. Order Controller Tests — `OrderControllerTest` (11 tests)

Integration tests for checkout and order management pages.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `checkoutPage_WithItems_Loads` | GET `/buyer/checkout` with cart items renders checkout page |
| 2 | `checkoutPage_EmptyCart_RedirectsToCart` | GET `/buyer/checkout` with empty cart redirects to `/buyer/cart` |
| 3 | `placeOrder_Success_RedirectsToOrders` | POST `/buyer/checkout` places order and redirects to orders |
| 4 | `placeOrder_Failure_RedirectsBackWithFlash` | Checkout failure redirects back with error flash message |
| 5 | `buyerOrders_LoadsPage` | GET `/buyer/orders` loads buyer orders page |
| 6 | `buyerOrderDetail_LoadsPage` | GET `/buyer/orders/1` loads order detail page |
| 7 | `cancelOrder_Success_RedirectsToOrders` | POST `/buyer/orders/1/cancel` cancels and redirects |
| 8 | `cancelOrder_NotPending_ShowsError` | Cancelling non-pending order shows error flash |
| 9 | `sellerOrders_LoadsPage` | GET `/seller/orders` loads seller order items page |
| 10 | `advanceStatus_Valid_Redirects` | POST `/seller/orders/1/advance` advances status and redirects |
| 11 | `advanceStatus_InvalidPermission_ShowsErrorFlash` | Seller cannot advance another seller's order item |

### 11. REST Order Controller Tests — `RestOrderControllerTest` (5 tests)

Integration tests for order REST API endpoints.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `checkout_Returns201` | POST `/api/buyer/orders/checkout` returns 201 Created |
| 2 | `getBuyerOrders_Returns200` | GET `/api/buyer/orders` returns 200 with orders JSON |
| 3 | `getBuyerOrderDetail_WrongBuyer_Returns403` | GET `/api/buyer/orders/100` for wrong buyer returns 403 |
| 4 | `getBuyerOrderDetail_Returns200` | GET `/api/buyer/orders/100` for correct buyer returns 200 |
| 5 | `getSellerOrders_Returns200` | GET `/api/seller/orders` returns 200 with order items JSON |

### 12. Seller Controller Tests — `SellerControllerTest` (11 tests)

Integration tests for seller dashboard, profile, and product CRUD.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `dashboard_LoadsWithProductCount` | GET `/seller/dashboard` loads with product count |
| 2 | `profilePage_LoadsWithProfileData` | GET `/seller/profile` renders profile page |
| 3 | `updateProfile_Valid_RedirectsWithSuccess` | Valid profile update redirects with success |
| 4 | `updateProfile_Invalid_ReturnsSamePage` | Invalid profile update (shop name too long) stays on page |
| 5 | `listProducts_LoadsPage` | GET `/seller/products` loads seller's product list |
| 6 | `showCreateForm_ApprovedSeller_ShowsForm` | Approved seller can see product create form |
| 7 | `showCreateForm_PendingSeller_RedirectsToDashboard` | Pending seller is redirected from create form |
| 8 | `createProduct_Valid_RedirectsToProducts` | Valid product creation redirects to product list |
| 9 | `showEditForm_OwnProduct_ShowsForm` | Seller can edit own product |
| 10 | `showEditForm_OtherSellersProduct_RedirectsToProducts` | Seller cannot edit another seller's product |
| 11 | `deleteProduct_RedirectsWithSuccess` | Product deletion redirects with success message |

### 13. Seller Profile Service Tests — `SellerProfileServiceTest` (7 tests)

Unit tests for `SellerProfileService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `createProfile_Success` | Creating seller profile saves correctly |
| 2 | `getProfileByUserId_Success` | Retrieves seller profile by user ID |
| 3 | `getProfileByUserId_NotFound` | Throws NotFoundException for missing profile |
| 4 | `updateProfile_Success` | Updating seller profile works |
| 5 | `approveSeller_ChangesStatusToApproved` | Admin approving seller changes status to APPROVED |
| 6 | `rejectSeller_ChangesStatusToRejected` | Admin rejecting seller changes status to REJECTED |
| 7 | `listByStatus_ReturnsCorrectSellers` | Filtering sellers by approval status works |

### 14. Admin Controller Tests — `AdminControllerTest` (8 tests)

Integration tests for admin dashboard and management pages.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `dashboard_LoadsWithStats` | GET `/admin/dashboard` loads with all dashboard statistics |
| 2 | `listSellers_LoadsPage` | GET `/admin/sellers` loads seller list page |
| 3 | `approveSeller_RedirectsWithSuccess` | POST `/admin/sellers/1/approve` approves and redirects |
| 4 | `rejectSeller_RedirectsWithSuccess` | POST `/admin/sellers/1/reject` rejects and redirects |
| 5 | `listUsers_LoadsPage` | GET `/admin/users` loads user list page |
| 6 | `listProducts_LoadsPage` | GET `/admin/products` loads product list page |
| 7 | `toggleProduct_RedirectsWithSuccess` | POST `/admin/products/1/toggle` toggles active and redirects |
| 8 | `listOrders_LoadsPage` | GET `/admin/orders` loads order list page |

### 15. REST Admin Controller Tests — `RestAdminControllerTest` (3 tests)

Integration tests for admin REST API endpoints.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `getStats_Returns200` | GET `/api/admin/stats` returns 200 with dashboard stats JSON |
| 2 | `listUsers_Returns200` | GET `/api/admin/users` returns 200 with user list JSON |
| 3 | `listPendingSellers_Returns200` | GET `/api/admin/sellers/pending` returns 200 with pending sellers |

### 16. Product Service Tests — `ProductServiceTest` (14 tests)

Unit tests for `ProductService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `findById_ReturnsCorrectProduct` | Finds product by ID |
| 2 | `findById_ThrowsNotFoundWhenMissing` | Throws NotFoundException for missing product |
| 3 | `create_ThrowsNotFoundForInvalidCategory` | Creating product with invalid category throws exception |
| 4 | `searchActive_ByNameWorks` | Searching active products by name works |
| 5 | `update_ThrowsAccessDeniedForOtherSeller` | Cannot update another seller's product |
| 6 | `update_UpdatesAndReturnsProduct` | Updating own product works |
| 7 | `delete_DeletesOwnProduct` | Deleting own product works |
| 8 | `delete_ThrowsAccessDeniedForOtherSeller` | Cannot delete another seller's product |
| 9 | `findActiveProducts_ReturnsActiveItems` | Listing active products works |
| 10 | `findActiveByCategory_Works` | Filtering active products by category works |
| 11 | `toggleActive_TogglesActiveStatus` | Toggling product active status works |
| 12 | `countBySeller_Works` | Counting products by seller works |
| 13 | `countActive_Works` | Counting active products works |
| 14 | `countTotal_Works` | Counting total products works |

### 17. Category Service Tests — `CategoryServiceTest` (8 tests)

Unit tests for `CategoryService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `findAll_ReturnsAllCategories` | Listing all categories works |
| 2 | `findById_ReturnsCorrectCategory` | Finding category by ID works |
| 3 | `findById_ThrowsNotFoundWhenMissing` | Throws NotFoundException for missing category |
| 4 | `findBySlug_ReturnsCorrectCategory` | Finding category by slug works |
| 5 | `findBySlug_ThrowsNotFoundWhenMissing` | Throws NotFoundException for missing slug |
| 6 | `create_ReturnsSavedCategory` | Creating a category saves and returns it |
| 7 | `existsByName_ReturnsTrueWhenExists` | Returns true when category name exists |
| 8 | `existsByName_ReturnsFalseWhenNotExists` | Returns false when category name doesn't exist |

### 18. Inventory Service Tests — `InventoryServiceTest` (6 tests)

Unit tests for `InventoryService` stock management logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `validateStock_PassesWhenEnoughStock` | Validation passes when stock is sufficient |
| 2 | `validateStock_FailsWhenInsufficientStock` | Throws InsufficientStockException when stock is low |
| 3 | `decreaseStock_Works` | Decreasing stock by a valid amount works |
| 4 | `decreaseStock_CannotGoBelowZero` | Cannot decrease stock below zero |
| 5 | `increaseStock_Works` | Increasing stock works |
| 6 | `multipleDecreases_ProduceCorrectFinalQuantity` | Multiple decreases produce correct final quantity |

### 19. Security Integration Tests — `SecurityIntegrationTest` (11 tests)

Full Spring Boot integration tests verifying role-based URL authorization rules using `@WithMockUser`.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `guest_CanAccessPublicProductList` | Unauthenticated user can browse `/products` |
| 2 | `guest_CanAccessApiProducts` | Unauthenticated user can access `/api/products` |
| 3 | `guest_CanAccessLoginPage` | Unauthenticated user can access `/login` |
| 4 | `guest_RedirectedFromBuyerDashboard` | Guest is redirected from `/buyer/dashboard` |
| 5 | `guest_RedirectedFromSellerDashboard` | Guest is redirected from `/seller/dashboard` |
| 6 | `guest_RedirectedFromAdminDashboard` | Guest is redirected from `/admin/dashboard` |
| 7 | `buyer_CanAccessBuyerDashboard` | BUYER role can access `/buyer/dashboard` |
| 8 | `buyer_BlockedFromSellerPages` | BUYER role gets 403 on `/seller/dashboard` |
| 9 | `buyer_BlockedFromAdminPages` | BUYER role gets 403 on `/admin/dashboard` |
| 10 | `seller_BlockedFromBuyerPages` | SELLER role gets 403 on `/buyer/dashboard` |
| 11 | `seller_BlockedFromAdminPages` | SELLER role gets 403 on `/admin/dashboard` |

### 20. Exception Handling Tests — `ExceptionHandlingTest` (10 tests)

Tests for `GlobalExceptionHandler` verifying correct error responses for both web (Thymeleaf views) and API (JSON) requests.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `notFound_Web_Returns404View` | NotFoundException → renders `error/404` view for web |
| 2 | `notFound_Api_Returns404Json` | NotFoundException → 404 JSON for API |
| 3 | `accessDenied_Web_Returns403View` | AccessDeniedException → renders `error/403` view for web |
| 4 | `accessDenied_Api_Returns403Json` | AccessDeniedException → 403 JSON for API |
| 5 | `insufficientStock_Web_ReturnsStockView` | InsufficientStockException → renders `error/stock` view for web |
| 6 | `insufficientStock_Api_Returns409Json` | InsufficientStockException → 409 Conflict JSON for API |
| 7 | `validation_Web_Returns400View` | ValidationException → renders `error/400` view for web |
| 8 | `validation_Api_Returns400Json` | ValidationException → 400 Bad Request JSON for API |
| 9 | `generic_Web_Returns500View` | RuntimeException → renders `error/500` view for web |
| 10 | `generic_Api_Returns500Json` | RuntimeException → 500 Internal Server Error JSON for API |

---

## Test Architecture

| Category | Approach | Framework |
|----------|----------|-----------|
| **Unit Tests** (Service layer) | Mockito mocks for repositories | JUnit 5 + Mockito |
| **Integration Tests** (Controllers) | `MockMvcBuilders.standaloneSetup()` with mocked services | JUnit 5 + MockMvc |
| **Security Tests** | `@SpringBootTest` + `@AutoConfigureMockMvc` + `@WithMockUser` | Spring Security Test |
| **Exception Tests** | Standalone MockMvc with dummy controller + `GlobalExceptionHandler` | JUnit 5 + MockMvc |

### Test Configuration

- **Test Database**: H2 in-memory (configured in `src/test/resources/application.yaml`)
- **Authentication in standalone tests**: Custom `HandlerMethodArgumentResolver` to inject mock `UserDetails` for `@AuthenticationPrincipal`
- **REST tests**: `GlobalExceptionHandler` attached as `@ControllerAdvice` to verify proper HTTP status codes

---

## Total Test Count by Module

| Module | Unit Tests | Integration Tests | Total |
|--------|-----------|-------------------|-------|
| Auth | 7 | 9 | 16 |
| Product | 28 | 9 | 37 |
| Buyer | 13 | 9 | 22 |
| Cart | 13 | 8 | 21 |
| Order | 1 | 16 | 17 |
| Seller | 7 | 11 | 18 |
| Admin | — | 11 | 11 |
| Security | — | 11 | 11 |
| Exception Handling | — | 10 | 10 |
| **Total** | **69** | **94** | **163** |

The project includes **163 tests** covering unit tests, integration tests, security authorization tests, and exception handling tests. All tests pass with **0 failures**.

```
Tests run: 163, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

### How to Run Tests

```bash
./mvnw test
```

---

## Test Breakdown by Module

### 1. Auth Controller Tests — `AuthControllerTest` (9 tests)

Integration tests for login and registration endpoints using MockMvc standalone setup.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `loginPage_Loads` | GET `/login` returns 200 and renders login view |
| 2 | `buyerRegisterPage_Loads` | GET `/register/buyer` returns 200 and renders buyer registration form |
| 3 | `sellerRegisterPage_Loads` | GET `/register/seller` returns 200 and renders seller registration form |
| 4 | `validBuyerRegistration_RedirectsToLogin` | POST `/register/buyer` with valid data redirects to `/login` |
| 5 | `validSellerRegistration_RedirectsToLogin` | POST `/register/seller` with valid data redirects to `/login` |
| 6 | `duplicateBuyerEmail_ShowsErrorOnForm` | Duplicate email during buyer registration returns form with error |
| 7 | `duplicateSellerEmail_ShowsErrorOnForm` | Duplicate email during seller registration returns form with error |
| 8 | `invalidBuyerForm_ReturnsSamePage` | Invalid buyer registration (blank fields) stays on same page |
| 9 | `invalidSellerForm_ReturnsSamePage` | Invalid seller registration (blank fields) stays on same page |

### 2. Auth Service Tests — `AuthServiceTest` (7 tests)

Unit tests for `AuthService` business logic with Mockito mocks.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `registerBuyer_Success` | Buyer registration creates user with BUYER role and buyer profile |
| 2 | `registerBuyer_EmailIsNormalized` | Email is converted to lowercase before saving |
| 3 | `registerBuyer_EmailExists` | Throws exception when email already exists |
| 4 | `registerBuyer_PasswordsDoNotMatch` | Throws exception when password and confirmPassword don't match |
| 5 | `registerSeller_Success` | Seller registration creates user with SELLER role and seller profile |
| 6 | `registerSeller_EmailExists` | Throws exception when email already exists |
| 7 | `registerSeller_PasswordsDoNotMatch` | Throws exception when passwords don't match |

### 3. Product Controller Tests — `ProductControllerTest` (6 tests)

Integration tests for public product browsing pages.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `listProducts_PublicProductListLoads` | GET `/products` returns 200 and renders product list |
| 2 | `listProducts_SearchQueryFiltersResults` | GET `/products?q=keyword` passes search query to service |
| 3 | `listProducts_CategoryFilterWorks` | GET `/products?categoryId=1` filters products by category |
| 4 | `productDetail_ValidProduct_Loads` | GET `/products/1` loads product detail page |
| 5 | `productDetail_MissingProduct_ReturnsErrorPage` | GET `/products/999` for nonexistent product returns 404 error page |
| 6 | `productDetail_InactiveProduct_NotShown` | Inactive product returns 404 error page |

### 4. REST Product Controller Tests — `RestProductControllerTest` (3 tests)

Integration tests for public REST product API endpoints.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `listProducts_Returns200` | GET `/api/products` returns 200 with success JSON |
| 2 | `getProduct_BadId_Returns404` | GET `/api/products/999` returns 404 JSON for missing product |
| 3 | `listCategories_Returns200` | GET `/api/categories` returns 200 with success JSON |

### 5. Buyer Controller Tests — `BuyerControllerTest` (9 tests)

Integration tests for buyer dashboard, profile, and address management.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `dashboard_LoadsForBuyer` | GET `/buyer/dashboard` renders buyer dashboard |
| 2 | `profile_LoadsProfilePage` | GET `/buyer/profile` renders profile page with user data |
| 3 | `updateProfile_ValidRequest_Redirects` | POST `/buyer/profile` with valid data redirects with success |
| 4 | `updateProfile_InvalidRequest_StaysOnPage` | Invalid profile update stays on profile page with errors |
| 5 | `addresses_LoadsPage` | GET `/buyer/addresses` loads addresses page |
| 6 | `addAddress_ValidRequest_Redirects` | POST `/buyer/addresses` with valid address redirects |
| 7 | `addAddress_InvalidRequest_StaysOnPage` | Invalid address submission stays on page |
| 8 | `setDefaultAddress_Works` | POST `/buyer/addresses/1/default` sets default and redirects |
| 9 | `deleteAddress_Works` | POST `/buyer/addresses/1/delete` deletes and redirects |

### 6. Buyer Profile Service Tests — `BuyerProfileServiceTest` (13 tests)

Unit tests for `BuyerProfileService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `createProfile_Success` | Creating a buyer profile saves correctly |
| 2 | `getProfileByUserId_Success` | Retrieves profile by user ID |
| 3 | `getProfileByUserId_NotFound` | Throws NotFoundException for missing profile |
| 4 | `updateProfile_Success` | Updating profile fields works |
| 5 | `getAddresses_Success` | Returns list of addresses for a buyer |
| 6 | `addAddress_FirstAddressBecomesDefault` | First address is automatically set as default |
| 7 | `addAddress_MakeDefaultClearsOldDefault` | Setting new default clears previous default |
| 8 | `updateAddress_Success` | Updating an address works |
| 9 | `updateAddress_AccessDenied` | Cannot update another buyer's address |
| 10 | `deleteAddress_Success` | Deleting own address works |
| 11 | `deleteAddress_AccessDenied` | Cannot delete another buyer's address |
| 12 | `setDefaultAddress_Success` | Setting default address works |
| 13 | `setDefaultAddress_AccessDenied` | Cannot set default on another buyer's address |

### 7. Cart Controller Tests — `CartControllerTest` (2 tests)

Integration tests for cart web pages.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `viewCart_LoadsPage` | GET `/buyer/cart` renders cart page |
| 2 | `addToCart_ValidRequest_Redirects` | POST `/buyer/cart/add` adds item and redirects |

### 8. REST Cart Controller Tests — `RestCartControllerTest` (6 tests)

Integration tests for cart REST API endpoints.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `getCart_Returns200` | GET `/api/cart` returns 200 with cart JSON |
| 2 | `addItem_Returns201` | POST `/api/cart/items` returns 201 Created |
| 3 | `updateItem_QuantityGreaterThanZero_Returns200` | PATCH `/api/cart/items/1` with quantity > 0 returns 200 |
| 4 | `updateItem_QuantityZeroOrLess_ReturnsNoContent` | PATCH `/api/cart/items/1` with quantity ≤ 0 returns 204 (removes item) |
| 5 | `removeItem_Returns204` | DELETE `/api/cart/items/1` returns 204 |
| 6 | `clearCart_Returns204` | DELETE `/api/cart` returns 204 |

### 9. Cart Service Tests — `CartServiceTest` (13 tests)

Unit tests for `CartService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `getOrCreateCart_ReturnsExistingCart` | Returns existing cart for user |
| 2 | `getOrCreateCart_CreatesMissingCart` | Creates new cart if none exists |
| 3 | `addItem_AddNewItemWorks` | Adding a new product to cart works |
| 4 | `addItem_AddingSameProductIncreasesQuantity` | Adding same product increases quantity |
| 5 | `addItem_ThrowsIfProductInactive` | Throws exception for inactive product |
| 6 | `addItem_ThrowsIfOutOfStock` | Throws exception for out-of-stock product |
| 7 | `updateItemQuantity_ValidNumberWorks` | Updating quantity to valid number works |
| 8 | `updateItemQuantity_AboveStockFails` | Updating quantity above stock throws exception |
| 9 | `updateItemQuantity_ZeroRemovesItem` | Setting quantity to 0 removes item |
| 10 | `updateItemQuantity_MissingItemThrowsNotFound` | Updating non-existent item throws NotFoundException |
| 11 | `removeItem_SpecificItemWorks` | Removing a specific item works |
| 12 | `clearCart_RemovesAllItems` | Clearing cart removes all items |
| 13 | `calculateTotal_CorrectWithSnapshot` | Total calculation uses price snapshot correctly |

### 10. Order Controller Tests — `OrderControllerTest` (11 tests)

Integration tests for checkout and order management pages.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `checkoutPage_WithItems_Loads` | GET `/buyer/checkout` with cart items renders checkout page |
| 2 | `checkoutPage_EmptyCart_RedirectsToCart` | GET `/buyer/checkout` with empty cart redirects to `/buyer/cart` |
| 3 | `placeOrder_Success_RedirectsToOrders` | POST `/buyer/checkout` places order and redirects to orders |
| 4 | `placeOrder_Failure_RedirectsBackWithFlash` | Checkout failure redirects back with error flash message |
| 5 | `buyerOrders_LoadsPage` | GET `/buyer/orders` loads buyer orders page |
| 6 | `buyerOrderDetail_LoadsPage` | GET `/buyer/orders/1` loads order detail page |
| 7 | `cancelOrder_Success_RedirectsToOrders` | POST `/buyer/orders/1/cancel` cancels and redirects |
| 8 | `cancelOrder_NotPending_ShowsError` | Cancelling non-pending order shows error flash |
| 9 | `sellerOrders_LoadsPage` | GET `/seller/orders` loads seller order items page |
| 10 | `advanceStatus_Valid_Redirects` | POST `/seller/orders/1/advance` advances status and redirects |
| 11 | `advanceStatus_InvalidPermission_ShowsErrorFlash` | Seller cannot advance another seller's order item |

### 11. REST Order Controller Tests — `RestOrderControllerTest` (5 tests)

Integration tests for order REST API endpoints.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `checkout_Returns201` | POST `/api/buyer/orders/checkout` returns 201 Created |
| 2 | `getBuyerOrders_Returns200` | GET `/api/buyer/orders` returns 200 with orders JSON |
| 3 | `getBuyerOrderDetail_WrongBuyer_Returns403` | GET `/api/buyer/orders/100` for wrong buyer returns 403 |
| 4 | `getBuyerOrderDetail_Returns200` | GET `/api/buyer/orders/100` for correct buyer returns 200 |
| 5 | `getSellerOrders_Returns200` | GET `/api/seller/orders` returns 200 with order items JSON |

### 12. Seller Controller Tests — `SellerControllerTest` (11 tests)

Integration tests for seller dashboard, profile, and product CRUD.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `dashboard_LoadsWithProductCount` | GET `/seller/dashboard` loads with product count |
| 2 | `profilePage_LoadsWithProfileData` | GET `/seller/profile` renders profile page |
| 3 | `updateProfile_Valid_RedirectsWithSuccess` | Valid profile update redirects with success |
| 4 | `updateProfile_Invalid_ReturnsSamePage` | Invalid profile update (shop name too long) stays on page |
| 5 | `listProducts_LoadsPage` | GET `/seller/products` loads seller's product list |
| 6 | `showCreateForm_ApprovedSeller_ShowsForm` | Approved seller can see product create form |
| 7 | `showCreateForm_PendingSeller_RedirectsToDashboard` | Pending seller is redirected from create form |
| 8 | `createProduct_Valid_RedirectsToProducts` | Valid product creation redirects to product list |
| 9 | `showEditForm_OwnProduct_ShowsForm` | Seller can edit own product |
| 10 | `showEditForm_OtherSellersProduct_RedirectsToProducts` | Seller cannot edit another seller's product |
| 11 | `deleteProduct_RedirectsWithSuccess` | Product deletion redirects with success message |

### 13. Seller Profile Service Tests — `SellerProfileServiceTest` (7 tests)

Unit tests for `SellerProfileService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `createProfile_Success` | Creating seller profile saves correctly |
| 2 | `getProfileByUserId_Success` | Retrieves seller profile by user ID |
| 3 | `getProfileByUserId_NotFound` | Throws NotFoundException for missing profile |
| 4 | `updateProfile_Success` | Updating seller profile works |
| 5 | `approveSeller_ChangesStatusToApproved` | Admin approving seller changes status to APPROVED |
| 6 | `rejectSeller_ChangesStatusToRejected` | Admin rejecting seller changes status to REJECTED |
| 7 | `listByStatus_ReturnsCorrectSellers` | Filtering sellers by approval status works |

### 14. Admin Controller Tests — `AdminControllerTest` (8 tests)

Integration tests for admin dashboard and management pages.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `dashboard_LoadsWithStats` | GET `/admin/dashboard` loads with all dashboard statistics |
| 2 | `listSellers_LoadsPage` | GET `/admin/sellers` loads seller list page |
| 3 | `approveSeller_RedirectsWithSuccess` | POST `/admin/sellers/1/approve` approves and redirects |
| 4 | `rejectSeller_RedirectsWithSuccess` | POST `/admin/sellers/1/reject` rejects and redirects |
| 5 | `listUsers_LoadsPage` | GET `/admin/users` loads user list page |
| 6 | `listProducts_LoadsPage` | GET `/admin/products` loads product list page |
| 7 | `toggleProduct_RedirectsWithSuccess` | POST `/admin/products/1/toggle` toggles active and redirects |
| 8 | `listOrders_LoadsPage` | GET `/admin/orders` loads order list page |

### 15. REST Admin Controller Tests — `RestAdminControllerTest` (3 tests)

Integration tests for admin REST API endpoints.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `getStats_Returns200` | GET `/api/admin/stats` returns 200 with dashboard stats JSON |
| 2 | `listUsers_Returns200` | GET `/api/admin/users` returns 200 with user list JSON |
| 3 | `listPendingSellers_Returns200` | GET `/api/admin/sellers/pending` returns 200 with pending sellers |

### 16. Product Service Tests — `ProductServiceTest` (14 tests)

Unit tests for `ProductService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `findById_ReturnsCorrectProduct` | Finds product by ID |
| 2 | `findById_ThrowsNotFoundWhenMissing` | Throws NotFoundException for missing product |
| 3 | `create_ThrowsNotFoundForInvalidCategory` | Creating product with invalid category throws exception |
| 4 | `searchActive_ByNameWorks` | Searching active products by name works |
| 5 | `update_ThrowsAccessDeniedForOtherSeller` | Cannot update another seller's product |
| 6 | `update_UpdatesAndReturnsProduct` | Updating own product works |
| 7 | `delete_DeletesOwnProduct` | Deleting own product works |
| 8 | `delete_ThrowsAccessDeniedForOtherSeller` | Cannot delete another seller's product |
| 9 | `findActiveProducts_ReturnsActiveItems` | Listing active products works |
| 10 | `findActiveByCategory_Works` | Filtering active products by category works |
| 11 | `toggleActive_TogglesActiveStatus` | Toggling product active status works |
| 12 | `countBySeller_Works` | Counting products by seller works |
| 13 | `countActive_Works` | Counting active products works |
| 14 | `countTotal_Works` | Counting total products works |

### 17. Category Service Tests — `CategoryServiceTest` (8 tests)

Unit tests for `CategoryService` business logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `findAll_ReturnsAllCategories` | Listing all categories works |
| 2 | `findById_ReturnsCorrectCategory` | Finding category by ID works |
| 3 | `findById_ThrowsNotFoundWhenMissing` | Throws NotFoundException for missing category |
| 4 | `findBySlug_ReturnsCorrectCategory` | Finding category by slug works |
| 5 | `findBySlug_ThrowsNotFoundWhenMissing` | Throws NotFoundException for missing slug |
| 6 | `create_ReturnsSavedCategory` | Creating a category saves and returns it |
| 7 | `existsByName_ReturnsTrueWhenExists` | Returns true when category name exists |
| 8 | `existsByName_ReturnsFalseWhenNotExists` | Returns false when category name doesn't exist |

### 18. Inventory Service Tests — `InventoryServiceTest` (6 tests)

Unit tests for `InventoryService` stock management logic.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `validateStock_PassesWhenEnoughStock` | Validation passes when stock is sufficient |
| 2 | `validateStock_FailsWhenInsufficientStock` | Throws InsufficientStockException when stock is low |
| 3 | `decreaseStock_Works` | Decreasing stock by a valid amount works |
| 4 | `decreaseStock_CannotGoBelowZero` | Cannot decrease stock below zero |
| 5 | `increaseStock_Works` | Increasing stock works |
| 6 | `multipleDecreases_ProduceCorrectFinalQuantity` | Multiple decreases produce correct final quantity |

### 19. Security Integration Tests — `SecurityIntegrationTest` (11 tests)

Full Spring Boot integration tests verifying role-based URL authorization rules using `@WithMockUser`.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `guest_CanAccessPublicProductList` | Unauthenticated user can browse `/products` |
| 2 | `guest_CanAccessApiProducts` | Unauthenticated user can access `/api/products` |
| 3 | `guest_CanAccessLoginPage` | Unauthenticated user can access `/login` |
| 4 | `guest_RedirectedFromBuyerDashboard` | Guest is redirected from `/buyer/dashboard` |
| 5 | `guest_RedirectedFromSellerDashboard` | Guest is redirected from `/seller/dashboard` |
| 6 | `guest_RedirectedFromAdminDashboard` | Guest is redirected from `/admin/dashboard` |
| 7 | `buyer_CanAccessBuyerDashboard` | BUYER role can access `/buyer/dashboard` |
| 8 | `buyer_BlockedFromSellerPages` | BUYER role gets 403 on `/seller/dashboard` |
| 9 | `buyer_BlockedFromAdminPages` | BUYER role gets 403 on `/admin/dashboard` |
| 10 | `seller_BlockedFromBuyerPages` | SELLER role gets 403 on `/buyer/dashboard` |
| 11 | `seller_BlockedFromAdminPages` | SELLER role gets 403 on `/admin/dashboard` |

### 20. Exception Handling Tests — `ExceptionHandlingTest` (10 tests)

Tests for `GlobalExceptionHandler` verifying correct error responses for both web (Thymeleaf views) and API (JSON) requests.

| # | Test | What It Verifies |
|---|------|-----------------|
| 1 | `notFound_Web_Returns404View` | NotFoundException → renders `error/404` view for web |
| 2 | `notFound_Api_Returns404Json` | NotFoundException → 404 JSON for API |
| 3 | `accessDenied_Web_Returns403View` | AccessDeniedException → renders `error/403` view for web |
| 4 | `accessDenied_Api_Returns403Json` | AccessDeniedException → 403 JSON for API |
| 5 | `insufficientStock_Web_ReturnsStockView` | InsufficientStockException → renders `error/stock` view for web |
| 6 | `insufficientStock_Api_Returns409Json` | InsufficientStockException → 409 Conflict JSON for API |
| 7 | `validation_Web_Returns400View` | ValidationException → renders `error/400` view for web |
| 8 | `validation_Api_Returns400Json` | ValidationException → 400 Bad Request JSON for API |
| 9 | `generic_Web_Returns500View` | RuntimeException → renders `error/500` view for web |
| 10 | `generic_Api_Returns500Json` | RuntimeException → 500 Internal Server Error JSON for API |

---

## Test Architecture

| Category | Approach | Framework |
|----------|----------|-----------|
| **Unit Tests** (Service layer) | Mockito mocks for repositories | JUnit 5 + Mockito |
| **Integration Tests** (Controllers) | `MockMvcBuilders.standaloneSetup()` with mocked services | JUnit 5 + MockMvc |
| **Security Tests** | `@SpringBootTest` + `@AutoConfigureMockMvc` + `@WithMockUser` | Spring Security Test |
| **Exception Tests** | Standalone MockMvc with dummy controller + `GlobalExceptionHandler` | JUnit 5 + MockMvc |

### Test Configuration

- **Test Database**: H2 in-memory (configured in `src/test/resources/application.yaml`)
- **Authentication in standalone tests**: Custom `HandlerMethodArgumentResolver` to inject mock `UserDetails` for `@AuthenticationPrincipal`
- **REST tests**: `GlobalExceptionHandler` attached as `@ControllerAdvice` to verify proper HTTP status codes

---

## Total Test Count by Module

| Module | Unit Tests | Integration Tests | Total |
|--------|-----------|-------------------|-------|
| Auth | 7 | 9 | 16 |
| Product | 28 | 9 | 37 |
| Buyer | 13 | 9 | 22 |
| Cart | 13 | 8 | 21 |
| Order | 1 | 16 | 17 |
| Seller | 7 | 11 | 18 |
| Admin | — | 11 | 11 |
| Security | — | 11 | 11 |
| Exception Handling | — | 10 | 10 |
| **Total** | **69** | **94** | **163** |
