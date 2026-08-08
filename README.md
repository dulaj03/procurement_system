# Procurement System - Enterprise Resource Planning

> **Note:** This is a work in progress. The system is currently undergoing migration from a legacy PHP platform to a modern microservices architecture with Angular frontend and Spring Boot backend. The `develop` branch contains the new implementation, while `main` may point to legacy code or placeholders.

## Overview

Procurement System is a comprehensive Enterprise Resource Planning (ERP) solution designed to streamline procurement processes, inventory management, and vendor relations. It features a modern, modular architecture built with:

- **Frontend**: Angular 22 with TypeScript and PrimeNG
- **Backend**: Java 21 with Spring Boot and Spring Security
- **Database**: PostgreSQL 16 with Spring Data JPA
- **Cache/Messaging**: Redis
- **Infrastructure**: Docker, GitHub Container Registry (GHCR), AWS ECS

## Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         Production Environment                           │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │                          AWS ECS Cluster                          │  │
│  │                                                                    │  │
│  │  ┌────────────────────┐   ┌────────────────────┐                │  │
│  │  │  Backend Service   │   │   Frontend Service │                │  │
│  │  │  (Spring Boot)     │   │   (Angular)        │                │  │
│  │  │                    │   │                    │                │  │
│  │  │  Task Definition   │   │  Task Definition   │                │  │
│  │  │  - Image:          │   │  - Image:          │                │  │
│  │  │    ghcr.io/          │   │    ghcr.io/          │                │  │
│  │  │    company/backend │   │    company/frontend│                │  │
│  │  │  Config:           │   │  Config:           │                │  │
│  │  │  - DB: Postgres    │   │  - API:             │                │  │
│  │  │  - Redis           │   │    https://api...  │                │  │
│  │  │  - Profile: prod   │   │  - Profile: prod   │                │  │
│  │  └────────────────────┘   └────────────────────┘                │  │
│  │                                                                    │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │                     GitHub Container Registry                     │  │
│  │                                                                    │  │
│  │  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐         │  │
│  │  │    Backend   │   │   Frontend   │   │   Common     │         │  │
│  │  │   Image:     │   │   Image:     │   │   Image:     │         │  │
│  │  │  repo/backend│   │  repo/frontend│   │  repo/common │         │  │
│  │  │  - Tags:     │   │  - Tags:     │   │  - Tags:     │         │  │
│  │  │    sha-xxxx  │   │    sha-xxxx  │   │    sha-xxxx  │         │  │
│  │  │    main      │   │    main      │   │    main      │         │  │
│  │  │    latest    │   │    latest    │   │    latest    │         │  │
│  │  └──────────────┘   └──────────────┘   └──────────────┘         │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘

```

## Modules

### Backend - Java/Spring Boot

**Features**:
- Authentication & Authorization (JWT-based)
- Multi-tenancy (multi-company, multi-branch)
- Role-based access control (RBAC)
- RESTful APIs with Swagger/OpenAPI documentation

**Key Components**:
- `AuthService`: User registration, login, JWT generation
- `CompanyService`: Company creation and management
- `BranchService`: Branch management within companies
- `UserService`: User management and profile
- `ProductService`: Product and category management
- `InventoryService`: Inventory tracking and stock management

**Endpoints**:
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/users/profile` - Get user profile
- `GET /api/companies` - List companies (admin)
- `POST /api/companies` - Create new company
- `GET /api/branches` - List branches (tenant)
- `POST /api/branches` - Create new branch
- `GET /api/products` - List products
- `POST /api/products` - Create new product
- `GET /api/inventory` - Inventory stock details
- `POST /api/inventory/stock-in` - Receive stock
- `POST /api/inventory/stock-out` - Issue stock

**Build Configuration**:
```bash
# Build and test
./mvnw clean install

# Build Docker image
./mvnw spring-boot:build-image
```

### Frontend - Angular 22

**Features**:
- Modern dashboard with PrimeNG components
- Responsive design with PrimeFlex
- Route-based lazy loading
- Authentication guards
- Company/branch selector
- Role-specific views

**Key Components**:
- `AuthService`: Login, register, token management
- `AuthGuard`: Route protection based on authentication
- `NoAuthGuard`: Prevent access to auth pages when logged in
- `TopbarComponent`: User info, notifications, branch selector
- `DashboardComponent`: Role-specific analytics
- `CompanyListComponent`: Company management
- `BranchListComponent`: Branch management
- `ProductListComponent`: Product catalog
- `InventoryListComponent`: Stock levels and movements

**Build Configuration**:
```bash
# Build production
npm run build -- --configuration=production

# Build Docker image
npm run build:docker
```

### Common - TypeScript Shared Code

Reusable TypeScript code shared between frontend and backend.

**Contents**:
```typescript
// package: @procure/common
{
  // Models
  "auth/models": { "AuthResponse", "LoginRequest", "RegisterRequest" },
  "company/models": { "Company", "CompanyCreateRequest", "CompanyResponse" },
  "branch/models": { "Branch", "BranchCreateRequest", "BranchResponse" },
  "product/models": { "Product", "ProductCreateRequest", "ProductResponse" },
  "inventory/models": { "InventoryItem", "StockInRequest", "StockOutRequest" },
  "user/models": { "UserInfo", "UserProfileUpdate" }
}
```

## Docker & Infrastructure

The system uses **GitHub Container Registry** for storing Docker images and **AWS ECS** for deployment.

### Docker Images

**Backend Image**:
```bash
# Build
docker build -t ghcr.io/company/backend:sha-xxxx .

# Push
docker push ghcr.io/company/backend:sha-xxxx
```

**Frontend Image**:
```bash
# Build
docker build -t ghcr.io/company/frontend:sha-xxxx .

# Push
docker push ghcr.io/company/frontend:sha-xxxx
```

### Task Definitions

**Frontend ECS Task Definition**:
```yaml
# procure-frontend task definition example
family: procure-frontend
containerDefinitions:
  - name: procure-frontend
    image: ghcr.io/company/frontend:sha-xxxx
    portMappings:
      - containerPort: 80
        protocol: tcp
    environment:
      - name: API_URL
        value: https://api.procure.com/api
    logConfiguration:
      logDriver: awslogs
      options:
        awslogs-group: /ecs/procure-frontend
        awslogs-region: us-east-1
```

**Backend ECS Task Definition**:
```yaml
#

