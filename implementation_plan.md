# Enterprise Procurement & Inventory Management Platform

## Overview

A full-stack, production-grade ERP/Procurement system with:
- **Backend**: Java 21 + Spring Boot 3.x (REST API, Security, JPA, Redis)
- **Frontend**: Angular 17+ (standalone components, lazy loading, PrimeNG/Angular Material)
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Infra**: Docker + Docker Compose + AWS (ECS/EC2, RDS, ElastiCache, S3, SES)
- **CI/CD**: GitHub Actions

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud                               │
│  ┌──────────┐    ┌──────────────────────────────────────────┐   │
│  │ Route 53 │───▶│ Application Load Balancer                │   │
│  └──────────┘    └───────────────┬──────────────────────────┘   │
│                                  │                              │
│          ┌───────────────────────┴──────────────────────┐       │
│          ▼                                               ▼       │
│  ┌──────────────┐                              ┌──────────────┐ │
│  │ ECS Angular  │                              │ ECS Spring   │ │
│  │  (Frontend)  │ ─── REST API (HTTPS) ──────▶│    Boot      │ │
│  └──────────────┘                              └──────┬───────┘ │
│                                                       │         │
│                          ┌────────────────────────────┘         │
│                          │                                      │
│              ┌───────────▼───────────┐  ┌────────────────────┐ │
│              │  RDS PostgreSQL 16    │  │ ElastiCache Redis  │ │
│              └───────────────────────┘  └────────────────────┘ │
│                                                                 │
│              ┌───────────────────────┐  ┌────────────────────┐ │
│              │  S3 (file storage)    │  │  SES (email)       │ │
│              └───────────────────────┘  └────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## Module Breakdown

| Module | Description |
|--------|-------------|
| `auth` | JWT auth, login, refresh token, password reset |
| `company` | Multi-company & multi-branch management |
| `user` | Employees, roles, RBAC |
| `supplier` | Supplier CRUD, contacts, ratings |
| `product` | Products, categories, units |
| `inventory` | Stock levels, warehouses, transfers |
| `purchase-request` | PR creation, multi-level approval |
| `purchase-order` | PO generation, supplier linkage |
| `receiving` | GRN (Goods Receipt Notes), stock update |
| `invoice` | Invoice management, payment tracking |
| `alert` | Low-stock alerts, notification triggers |
| `dashboard` | Analytics aggregations, KPI data |
| `audit` | Audit log for all entity changes |
| `notification` | Email (SES) + in-app notifications |
| `report` | Exportable reports (PDF, Excel) |

---

## Proposed Folder Structure

```
procurement_system/
│
├── backend/                          # Spring Boot Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/procure/
│   │   │   │   ├── ProcurementApplication.java
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── RedisConfig.java
│   │   │   │   │   ├── AwsConfig.java
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   ├── MailConfig.java
│   │   │   │   │   └── OpenApiConfig.java
│   │   │   │   ├── common/
│   │   │   │   │   ├── audit/          # Spring Data Auditing
│   │   │   │   │   ├── exception/      # GlobalExceptionHandler
│   │   │   │   │   ├── response/       # ApiResponse wrapper
│   │   │   │   │   ├── pagination/     # PageRequest helpers
│   │   │   │   │   └── util/           # JwtUtil, etc.
│   │   │   │   ├── module/
│   │   │   │   │   ├── auth/
│   │   │   │   │   ├── company/
│   │   │   │   │   ├── user/
│   │   │   │   │   ├── supplier/
│   │   │   │   │   ├── product/
│   │   │   │   │   ├── inventory/
│   │   │   │   │   ├── purchase/
│   │   │   │   │   │   ├── request/    # Purchase Requests + Approvals
│   │   │   │   │   │   └── order/      # Purchase Orders
│   │   │   │   │   ├── receiving/
│   │   │   │   │   ├── invoice/
│   │   │   │   │   ├── alert/
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   ├── audit/
│   │   │   │   │   ├── notification/
│   │   │   │   │   └── report/
│   │   │   │   └── security/
│   │   │   │       ├── JwtAuthFilter.java
│   │   │   │       ├── JwtService.java
│   │   │   │       └── UserDetailsServiceImpl.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/migration/       # Flyway migrations
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                          # Angular Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/
│   │   │   │   ├── auth/              # Auth guards, interceptors
│   │   │   │   ├── services/          # API services
│   │   │   │   ├── models/            # TypeScript interfaces
│   │   │   │   └── interceptors/      # HTTP interceptors
│   │   │   ├── shared/
│   │   │   │   ├── components/        # Reusable components
│   │   │   │   ├── directives/
│   │   │   │   └── pipes/
│   │   │   ├── layout/
│   │   │   │   ├── sidebar/
│   │   │   │   ├── topbar/
│   │   │   │   └── main-layout/
│   │   │   └── features/
│   │   │       ├── auth/
│   │   │       ├── dashboard/
│   │   │       ├── companies/
│   │   │       ├── users/
│   │   │       ├── suppliers/
│   │   │       ├── products/
│   │   │       ├── inventory/
│   │   │       ├── purchase-requests/
│   │   │       ├── purchase-orders/
│   │   │       ├── receiving/
│   │   │       ├── invoices/
│   │   │       └── reports/
│   │   ├── assets/
│   │   ├── environments/
│   │   └── styles/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── infra/                            # Infrastructure as Code
│   ├── docker/
│   │   └── docker-compose.yml        # Local dev stack
│   ├── aws/
│   │   ├── ecs-task-definition.json
│   │   ├── rds-setup.sh
│   │   └── elasticache-setup.sh
│   └── scripts/
│       ├── init-db.sql
│       └── seed-data.sql
│
├── .github/
│   └── workflows/
│       ├── backend-ci.yml
│       └── frontend-ci.yml
│
└── README.md
```

---

## Phased Build Plan (Step-by-Step)

### ✅ Phase 1 — Foundation (We do this NOW)
1. Scaffold project structure (folders, placeholder files)
2. Create `docker-compose.yml` for local dev (Postgres, Redis)
3. Initialize Spring Boot project (`pom.xml`, main class, configs)
4. Setup `application.yml` with datasource, Redis, JWT config
5. Initialize Angular project with routing, lazy modules, PrimeNG

### 🔲 Phase 2 — Auth & RBAC
6. DB migrations: `users`, `roles`, `permissions`, `companies`, `branches`
7. Spring Security + JWT (login, refresh, logout)
8. Angular Auth module (login page, guards, interceptors)

### 🔲 Phase 3 — Company, User, Supplier
9. Multi-company/branch entity + APIs
10. User/Employee CRUD + role assignment
11. Supplier management

### 🔲 Phase 4 — Product & Inventory
12. Product/category/unit management
13. Warehouse and stock management
14. Stock transfer between branches

### 🔲 Phase 5 — Procurement Flow
15. Purchase Requests + multi-level approval workflow
16. Purchase Orders generated from approved PRs
17. Goods Receipt Notes (stock receiving)

### 🔲 Phase 6 — Finance & Alerts
18. Invoice management + payment tracking
19. Low-stock alert engine (Redis pub/sub or scheduled job)
20. Email notifications via AWS SES

### 🔲 Phase 7 — Dashboard & Reports
21. Analytics APIs (sales, purchases, inventory)
22. Dashboard widgets in Angular
23. Exportable PDF/Excel reports

### 🔲 Phase 8 — Infrastructure & Deployment
24. Dockerfiles for backend and frontend
25. GitHub Actions CI/CD pipeline
26. AWS ECS + RDS + ElastiCache deployment

---

## What I Need From You (Step 1)

Before we write code, please confirm or clarify:

1. **Java version**: Java 21 (LTS recommended) — OK?
2. **Build tool**: Maven — OK, or do you prefer Gradle?
3. **Angular version**: Angular 18 (latest stable) — OK?
4. **UI Library**: PrimeNG or Angular Material for the frontend UI?
5. **Database name/schema name**: e.g., `procure_db`
6. **JWT secret strategy**: Random secret in `.env` or AWS Secrets Manager?
7. **Email**: Do you already have an AWS account/SES configured, or use a placeholder SMTP for now?
8. **Domain name** (optional): Any custom domain, or we use default AWS URLs for now?

> [!IMPORTANT]
> Once you answer the above, I will generate ALL the base files simultaneously: `pom.xml`, `application.yml`, `docker-compose.yml`, Angular scaffold, and all config classes — ready to run.
