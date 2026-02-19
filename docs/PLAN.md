# POS System - Full Production Plan

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | Angular 17 + Angular Material + TypeScript |
| **Backend** | Spring Boot 3 + Java 21 |
| **Database** | PostgreSQL |
| **Auth** | Spring Security + JWT |
| **CI/CD** | GitHub Actions |
| **Hosting** | Azure (App Service + Static Web Apps + PostgreSQL) |

---

## Architecture

```
┌─────────────────────────────────────────┐
│     Angular 17 + Angular Material       │
│   POS Screen │ Admin │ Reports │ Auth   │
└──────────────────┬──────────────────────┘
                   │ HTTP REST (JSON + JWT)
┌──────────────────▼──────────────────────┐
│         Spring Boot 3 (Java 21)         │
│  /api/products  /api/orders  /api/auth  │
└──────────────────┬──────────────────────┘
                   │ Spring Data JPA
┌──────────────────▼──────────────────────┐
│              PostgreSQL                 │
│  products │ orders │ users │ inventory  │
└─────────────────────────────────────────┘
```

---

## Project Structure

```
cicdpos-2026/
├── CicdApi/          ← Spring Boot REST API (backend)
│   ├── src/
│   ├── docs/         ← this folder
│   ├── build.gradle
│   └── .github/workflows/ci.yml
└── CicdUI/           ← Angular app (frontend)
    ├── src/
    ├── angular.json
    └── .github/workflows/ci.yml
```

---

## Build Order

### Phase 1 — Backend (Spring Boot REST API)
- [ ] 1.1 Convert existing app to Spring Boot 3
- [ ] 1.2 Set up PostgreSQL + Spring Data JPA
- [ ] 1.3 Create database schema (entities)
- [ ] 1.4 Build REST endpoints
  - [ ] `/api/auth` — login, register, JWT
  - [ ] `/api/products` — CRUD, categories
  - [ ] `/api/inventory` — stock management
  - [ ] `/api/orders` — cart, checkout
  - [ ] `/api/customers` — customer management
  - [ ] `/api/reports` — sales, revenue
- [ ] 1.5 Spring Security + JWT roles (Admin, Manager, Cashier)
- [ ] 1.6 Unit + integration tests
- [ ] 1.7 CI/CD → Azure App Service

### Phase 2 — Database (PostgreSQL)
- [ ] 2.1 Design schema (ERD)
- [ ] 2.2 JPA entities + repositories
- [ ] 2.3 Flyway DB migrations
- [ ] 2.4 Azure PostgreSQL Flexible Server setup
- [ ] 2.5 Seed data (sample products, admin user)

### Phase 3 — Frontend (Angular)
- [ ] 3.1 Create Angular 17 project (CicdUI)
- [ ] 3.2 Set up Angular Material + routing
- [ ] 3.3 Auth module (login page, JWT interceptor, guards)
- [ ] 3.4 POS Screen (main cashier screen)
  - [ ] Product grid / barcode search
  - [ ] Cart sidebar
  - [ ] Checkout + payment
  - [ ] Receipt print
- [ ] 3.5 Admin module
  - [ ] Product management (CRUD)
  - [ ] Inventory management
  - [ ] User management
- [ ] 3.6 Reports module
  - [ ] Daily sales dashboard
  - [ ] Revenue charts (Chart.js)
  - [ ] Top products
- [ ] 3.7 CI/CD → Azure Static Web Apps

### Phase 4 — Auth & Security
- [ ] 4.1 JWT login/logout
- [ ] 4.2 Role-based access (Admin / Manager / Cashier)
- [ ] 4.3 Route guards (Angular)
- [ ] 4.4 API security (Spring Security)
- [ ] 4.5 Password encryption (BCrypt)

### Phase 5 — Payments
- [ ] 5.1 Cash payment flow
- [ ] 5.2 Stripe integration (card payments)
- [ ] 5.3 Payment receipts (PDF)

### Phase 6 — Reports & Analytics
- [ ] 6.1 Daily / weekly / monthly sales
- [ ] 6.2 Revenue by product / category
- [ ] 6.3 Inventory alerts (low stock)
- [ ] 6.4 Export (CSV / PDF)

### Phase 7 — Production Hardening
- [ ] 7.1 Error handling (global exception handler)
- [ ] 7.2 Logging (SLF4J + Azure Monitor)
- [ ] 7.3 API rate limiting
- [ ] 7.4 HTTPS + CORS configuration
- [ ] 7.5 Environment configs (dev / staging / prod)
- [ ] 7.6 Azure deployment slots (blue/green deploy)

---

## Database Schema (ERD overview)

```
users
├── id, username, password, role, created_at

categories
├── id, name, description

products
├── id, name, sku, barcode, price, category_id, image_url, active

inventory
├── id, product_id, quantity, low_stock_threshold, updated_at

customers
├── id, name, email, phone, created_at

orders
├── id, customer_id, cashier_id, total, tax, discount, status, created_at

order_items
├── id, order_id, product_id, quantity, unit_price, subtotal

payments
├── id, order_id, method, amount, status, transaction_id, created_at
```

---

## Azure Deployment

| Service | Plan | Cost |
|---------|------|------|
| **App Service** (API) | F1 Free | $0 |
| **Static Web Apps** (UI) | Free tier | $0 |
| **PostgreSQL Flexible** | Free tier | $0 |
| **Total** | | **$0/month** |

---

## CI/CD Pipelines

### Backend pipeline (CicdApi)
```
push to main → build → test → deploy to Azure App Service
```

### Frontend pipeline (CicdUI)
```
push to main → npm install → ng build → deploy to Azure Static Web Apps
```

---

## Modules Summary

| Module | Description |
|--------|-------------|
| **Auth** | Login, JWT, roles |
| **POS Screen** | Main cashier interface |
| **Products** | Product & category management |
| **Inventory** | Stock tracking & alerts |
| **Orders** | Cart, checkout, receipts |
| **Customers** | Customer profiles |
| **Payments** | Cash + Stripe card |
| **Reports** | Sales analytics & charts |
| **Admin** | User & system management |
