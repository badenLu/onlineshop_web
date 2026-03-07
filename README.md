# 🛒 OnlineShop — E-Commerce Platform

> A production-grade full-stack e-commerce platform demonstrating enterprise-level architecture, high-concurrency inventory management, and database optimization strategies.

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?logo=react)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.0-red?logo=redis)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.11-yellow?logo=elasticsearch)
![Kafka](https://img.shields.io/badge/Kafka-7.5-black?logo=apachekafka)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![CI](https://img.shields.io/github/actions/workflow/status/yourusername/onlineshop/ci.yml?label=CI&logo=githubactions)

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Database Design](#database-design)
- [Key Technical Highlights](#key-technical-highlights)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Frontend-Backend Integration](#frontend-backend-integration)
- [Testing](#testing)
- [CI/CD & Deployment](#cicd--deployment)
- [Project Structure](#project-structure)

---

## Overview

This project demonstrates:

- **Domain-Driven Design (DDD)** — Bounded contexts (User, Product, Order), each with independent entity/service/controller layers, ready for microservice extraction
- **JWT Authentication** — Access + refresh tokens, Redis-based token blacklist for secure logout
- **Inventory Oversell Prevention** — Redis Lua atomic scripts + MySQL optimistic locking (double-layer defense)
- **Order Lifecycle** — Full state machine with idempotent creation, auto-cancel for unpaid orders (30min TTL)
- **Caching Strategy** — Multi-level caching with Redis (product detail, category tree, shopping cart)
- **Database Engineering** — Flyway migrations, indexing strategy, read-write splitting design, horizontal scaling design
- **Production-Ready DevOps** — Docker Compose orchestration, GitHub Actions CI/CD, Kubernetes manifests

---

## Tech Stack

### Backend

| Technology | Purpose |
|---|---|
| Spring Boot 3.2 | Core framework, REST API |
| Spring Security + JWT | Authentication & authorization |
| Spring Data JPA (Hibernate) | ORM / data access |
| MySQL 8.0 (InnoDB) | Primary relational database |
| Redis 7.0 | Caching, shopping cart, distributed lock, rate limiting |
| Elasticsearch 8.11 | Product full-text search |
| Apache Kafka | Async event processing |
| Flyway | Database migration & version control |
| Redisson | Distributed lock implementation |

### Frontend

| Technology | Purpose |
|---|---|
| React 18 + TypeScript | SPA framework with type safety |
| Ant Design 5 | UI component library |
| Axios | HTTP client with interceptors |
| Redux Toolkit | Global state management |
| React Router 6 | Client-side routing |

### DevOps & Quality

| Technology | Purpose |
|---|---|
| Docker Compose | One-command full-stack deployment |
| GitHub Actions | CI/CD pipeline |
| JUnit 5 + Mockito | Unit testing |
| JaCoCo | Code coverage |
| Kubernetes | Production deployment manifests |

---

## Architecture

                          ┌--------------┐
                          │    Nginx     │
                          │  (Reverse    │
                          │   Proxy)     │
                          └──────┬───────┘
                                 │
                ┌────────────────┼────────────────┐
                │                                  │
         ┌──────▼──────┐                   ┌──────▼──────┐
         │  React SPA  │                   │ Spring Boot  │
         │  (Port 3000)│                   │ (Port 8080)  │
         └─────────────┘                   └──────┬───────┘
                                                  │
             ┌────────────┬──────────┬────────────┼──────────┐
             │            │          │            │          │
      ┌──────▼───┐ ┌─────▼───┐ ┌───▼──────┐   ┌──▼────┐  ┌──▼──────┐
      │  MySQL   │ │  Redis  │ │  Elastic     │ Kafka │  │  MinIO  │
      │  Master  │ │         │ │  search      │       │  │ (Files) │
      └──────┬───┘ └─────────┘ └──────────┘   └───────┘  └─────────┘
             │
      ┌──────▼────┐
      │  MySQL    │
      │  Slave    │  ← Read-Write Splitting (design below)
      └───────────┘

### Request Flow
```
Client Request
     │
     ▼
┌─────────────────────────────────────────┐
│  Spring Security Filter Chain           │
│  ├── JwtAuthenticationFilter            │
│  │   (validate token, check blacklist)  │
│  └── Authorization (RBAC)               │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│  Controller Layer (@Valid)              │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│  Service Layer                          │
│  ├── Redis Cache (Cache-Aside)          │
│  ├── Distributed Lock (Redisson)        │
│  ├── Kafka Producer (async events)      │
│  └── @Transactional                     │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│  Repository Layer (Spring Data JPA)     │
│  └── MySQL (Master: write / Slave: read)│
└─────────────────────────────────────────┘
```

### Microservices Evolution Plan

The project follows a modular monolith pattern, designed for easy extraction into microservices:
```
┌──────────────┐  ┌───────────────┐  ┌──────────────┐
│ User Service │  │Product Service│  │ Order Service │
│  /api/auth   │  │ /api/products │  │  /api/orders  │
│  /api/users  │  │ /api/categories│ │  /api/cart    │
└──────┬───────┘  └──────┬────────┘ └──────┬────────┘
       │                 │                  │
       └─────────────────┼──────────────────┘
                         │
                   ┌─────▼─────┐
                   │   Kafka   │
                   │  (async   │
                   │  events)  │
                   └───────────┘

Each bounded context has independent:
- Entity & Repository layer
- Service layer with business logic
- REST Controller
- Can be extracted to a standalone service with its own database
```

---

## Database Design

### ER Diagram
```
┌──────────────┐       ┌───────────────┐       ┌──────────────────┐
│    users     │       │  categories   │       │    products      │
├──────────────┤       ├───────────────┤       ├──────────────────┤
│ PK id        │       │ PK id         │       │ PK id            │
│    username  │       │    name       │       │ FK category_id   │
│    email  UQ │       │ FK parent_id  │       │    name          │
│    password  │       │    level      │       │    description   │
│    phone     │       │    sort_order │       │    price         │
│    avatar    │       │    icon_url   │       │    original_price│
│    role      │       │    status     │       │    stock         │
│    status    │       └───────────────┘       │    sales_count   │
│    created_at│                               │    main_image    │
│    updated_at│                               │    images (JSON) │
└──────┬───────┘                               │    status        │
       │                                       └────────┬─────────┘
       │        ┌───────────────┐                       │
       │        │ product_skus  │                       │
       │        ├───────────────┤                       │
       │        │ PK id         │                       │
       │        │ FK product_id ┼───────────────────────┘
       │        │    sku_code UQ│
       │        │    attributes │  ← JSON {"color":"Red","size":"XL"}
       │        │    price      │
       │        │    stock      │
       │        └───────┬───────┘
       │                │
       │   ┌────────────┼───────────────────────────────┐
       │   │            │                               │
       │   │   ┌────────▼────────┐             ┌────────▼───────┐
       │   │   │  shopping_cart  │             │   order_items  │
       │   │   ├─────────────────┤             ├────────────────┤
       │   │   │ FK user_id      │             │ FK order_id    │
       │   │   │ FK sku_id       │             │ FK product_id  │
       │   │   │    quantity     │             │ FK sku_id      │
       │   │   │    selected     │             │    product_name│ ← snapshot
       │   │   └─────────────────┘             │    unit_price  │ ← snapshot
       │   │                                   │    quantity    │
       │   │   ┌─────────────────┐             │    total_price │
       │   │   │     orders      │             └────────────────┘
       │   │   ├─────────────────┤
       ├───┼──→│ FK user_id      │
       │   │   │    order_no  UQ │
       │   │   │    total_amount │
       │   │   │    pay_amount   │
       │   │   │    freight      │
       │   │   │    status       │ ← state machine
       │   │   │    payment_type │
       │   │   │    receiver_*   │
       │   │   │    created_at   │
       │   │   └─────────────────┘
       │   │
       │   │   ┌──────────────────┐     ┌──────────────────┐
       │   │   │ user_addresses   │     │ payment_records  │
       │   │   ├──────────────────┤     ├──────────────────┤
       └───┼──→│ FK user_id       │     │ FK order_id      │
           │   │    receiver_name │     │    payment_no    │
           │   │    phone         │     │    payment_type  │
           │   │    province/city │     │    amount        │
           │   │    detail_addr   │     │    status        │
           │   │    is_default    │     │    callback_data │
           │   └──────────────────┘     └──────────────────┘
```

### Indexing Strategy
```sql
-- User login (most frequent auth query)
UNIQUE INDEX uk_user_email ON users(email);

-- Product browsing by category
INDEX idx_product_category_status ON products(category_id, status, created_at DESC);

-- Product ranking by sales
INDEX idx_product_sales ON products(sales_count DESC);

-- User's order history (every user checks this)
INDEX idx_order_user_status ON orders(user_id, status, created_at DESC);

-- Payment callback lookup
UNIQUE INDEX uk_order_no ON orders(order_no);

-- Cart items per user (unique constraint prevents duplicates)
UNIQUE INDEX uk_cart_user_sku ON shopping_cart(user_id, sku_id);
```

### Slow Query Optimization

| Scenario | Problem | Solution |
|---|---|---|
| Product list with filters | Full table scan with OR conditions | Elasticsearch for complex search |
| Order pagination page 10000+ | `OFFSET 200000` performance cliff | Cursor-based: `WHERE id > #{lastId} LIMIT 20` |
| Real-time sales ranking | COUNT + GROUP BY on large table | Redis ZSET, hourly DB sync |
| Fuzzy search `LIKE '%keyword%'` | Cannot use B-tree index | Elasticsearch with analyzers |

### Read-Write Splitting Design
```
                    ┌─────────────────┐
                    │   Application   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  Dynamic Data   │
                    │  Source Router  │
                    │  @Master → write│
                    │  @Slave  → read │
                    └───┬─────────┬───┘
                   Write│         │Read
                 ┌──────▼──┐  ┌──▼───────┐
                 │  Master │→→│  Slave   │
                 │  MySQL  │  │  MySQL   │
                 └─────────┘  └──────────┘
                   binlog → relay log

Key decisions:
- Write operations → Master
- Read operations → Slave (default)
- Critical reads after write (e.g. order status post-payment)
  → Force Master to avoid replication lag
```

### Horizontal Scaling Design
```
Shard key: user_id (single-user queries never cross shards)
Strategy:  user_id % 4 → orders_00 ~ orders_03

Order number encodes shard info:
  {yyyyMMddHHmmss}{shard_id:02d}{sequence:06d}

Cross-shard queries (admin search):
  → Elasticsearch as query engine (synced via Debezium CDC)
```

---

## Key Technical Highlights

### 1. Inventory Oversell Prevention

Three-layer defense for flash sale scenarios:
```
Layer 1: Redis Lua Script (atomic check + deduct, no race condition)
  ↓ success
Layer 2: DB Optimistic Lock (WHERE stock >= quantity)
  ↓ fail → compensate Redis
Layer 3: Auto-cancel unpaid orders after 30min → restore stock
```
```java
// Redis Lua: atomic stock deduction
private static final String DEDUCT_STOCK_LUA = """
    local stock = redis.call('GET', KEYS[1])
    if stock == false then return -1 end
    if tonumber(stock) >= tonumber(ARGV[1]) then
        redis.call('DECRBY', KEYS[1], ARGV[1])
        return 1
    end
    return 0
    """;

// MySQL: optimistic lock fallback
@Query("UPDATE ProductSku s SET s.stock = s.stock - :qty " +
       "WHERE s.id = :skuId AND s.stock >= :qty")
int deductStock(@Param("skuId") Long skuId, @Param("qty") int qty);
```

### 2. Order State Machine
```
PENDING_PAYMENT ──pay──→ PAID ──ship──→ SHIPPED ──confirm──→ COMPLETED
       │                                    │
       │ (30min timeout)                    │ (admin)
       ▼                                    ▼
   CANCELLED                            DELIVERED
       ↑
       │ (user cancel)
   PENDING_PAYMENT
```

### 3. Redis Usage Patterns

| Pattern | Data Structure | Use Case |
|---|---|---|
| Cache-Aside | String + TTL | Product detail (30min), Category tree (1h) |
| Distributed Lock | Redisson RLock | Flash sale inventory deduction |
| Shopping Cart | Hash | `cart:{userId}` → `{skuId: {qty, selected}}` |
| Token Blacklist | String + TTL | JWT logout invalidation |
| Idempotency | SETNX + TTL | Prevent duplicate order submission |
| Stock Cache | String | `inventory:sku:{id}` → stock count |

### 4. JWT Authentication Flow
```
Register/Login → [accessToken + refreshToken]
    │
    ▼ (every request)
Authorization: Bearer {accessToken}
    │
    ▼
JwtAuthenticationFilter:
  1. Extract token from header
  2. Validate signature + expiration
  3. Check Redis blacklist (logout?)
  4. Set SecurityContext
    │
    ▼ (token expired)
POST /api/auth/refresh with X-Refresh-Token header
    │
    ▼ (logout)
POST /api/auth/logout → token added to Redis blacklist
```

---

## Getting Started

### Prerequisites

- Docker & Docker Compose v2
- Or manually: JDK 17+, Node.js 18+, MySQL 8.0, Redis 7.0

### One-Command Start
```bash
git clone https://github.com/yourusername/onlineshop.git
cd onlineshop

docker compose up -d

# Frontend:    http://localhost:3000
# Backend API: http://localhost:8080
# Swagger UI:  http://localhost:8080/swagger-ui.html
```

### Local Development
```bash
# 1. Start infrastructure only
docker compose up -d mysql redis kafka elasticsearch

# 2. Backend
cd backend
./gradlew bootRun

# 3. Frontend
cd frontend
npm install
npm run dev    # http://localhost:5173
```

### Test Accounts

| Role | Email | Password |
|---|---|---|
| Admin | admin@shop.com | admin123 |
| User | test@test.com | 123456 |

---

## API Reference

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register new user |
| POST | `/api/auth/login` | No | Login → JWT tokens |
| POST | `/api/auth/refresh` | Refresh Token | Refresh access token |
| POST | `/api/auth/logout` | Yes | Blacklist current token |

### Products (Public)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products?page=1&size=20&categoryId=1&sort=price_asc` | Product list |
| GET | `/api/products/{id}` | Product detail with SKUs |
| GET | `/api/products/search?q=keyword` | Full-text search |
| GET | `/api/categories` | Category tree |

### Shopping Cart

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/cart` | Get my cart |
| POST | `/api/cart` | Add item `{skuId, quantity}` |
| PUT | `/api/cart/{skuId}` | Update quantity |
| DELETE | `/api/cart/{skuId}` | Remove item |
| DELETE | `/api/cart` | Clear cart |

### Orders

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Create order (idempotent) |
| GET | `/api/orders?status=PAID&page=1` | My orders |
| GET | `/api/orders/{orderNo}` | Order detail |
| PUT | `/api/orders/{orderNo}/cancel` | Cancel (pending only) |
| POST | `/api/orders/{orderNo}/pay` | Pay order |
| PUT | `/api/orders/{orderNo}/confirm` | Confirm receipt |

### Admin

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/admin/products` | Create product |
| PUT | `/api/admin/products/{id}` | Update product |
| DELETE | `/api/admin/products/{id}` | Soft delete product |
| GET | `/api/admin/orders` | All orders |
| PUT | `/api/admin/orders/{id}/ship` | Ship order |

### Response Format
```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1703001234567
}
```

---

## Frontend-Backend Integration

### Axios Interceptors
```typescript
// Request: inject JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response: auto-refresh on 401
api.interceptors.response.use(
  (res) => res.data,
  async (err) => {
    if (err.response?.status === 401) {
      const refreshed = await tryRefreshToken();
      if (refreshed) return api(err.config);
      redirectToLogin();
    }
    return Promise.reject(err);
  }
);
```

### Environment Setup
```
Development:  Vite proxy /api → localhost:8080 (no CORS issues)
Production:   Nginx reverse proxy /api/ → backend:8080
```

---

## Testing
```bash
cd backend
./gradlew test                    # Run all tests
./gradlew jacocoTestReport        # Generate coverage report
```

| Layer | Strategy | Focus |
|---|---|---|
| Service | JUnit 5 + Mockito | Business logic, edge cases, error handling |
| Inventory | Mockito | Redis Lua script behavior, cache miss recovery |
| Order | Mockito | State transitions, stock rollback, idempotency |

---

## CI/CD & Deployment

### GitHub Actions Pipeline
```
Push / PR to main
     │
     ├── Backend: build → test → coverage report
     ├── Frontend: lint → type check → build
     └── Upload artifacts
```

### Kubernetes

Production-ready manifests in `k8s/` directory with:
- Deployment with 2 replicas and resource limits
- Readiness probes on `/actuator/health`
- ConfigMap / Secret for environment variables
- LoadBalancer service for frontend

---

## Project Structure
```
onlineshop/
├── backend/
│   ├── src/main/java/com/ecommerce/
│   │   ├── user/                  # User bounded context
│   │   │   ├── entity/            #   User, UserAddress
│   │   │   ├── dto/               #   AuthRequest, AuthResponse
│   │   │   ├── repository/        #   UserRepository
│   │   │   ├── service/impl/      #   AuthServiceImpl
│   │   │   └── controller/        #   AuthController
│   │   ├── product/               # Product bounded context
│   │   │   ├── entity/            #   Product, ProductSku, Category
│   │   │   ├── dto/               #   ProductRequest/Response
│   │   │   ├── repository/        #   ProductRepository, SkuRepository
│   │   │   ├── service/impl/      #   ProductService, InventoryService
│   │   │   └── controller/        #   ProductController, AdminProductController
│   │   ├── order/                 # Order bounded context
│   │   │   ├── entity/            #   Order, OrderItem
│   │   │   ├── dto/               #   OrderRequest/Response, CartRequest
│   │   │   ├── repository/        #   OrderRepository
│   │   │   ├── service/impl/      #   OrderService, CartService
│   │   │   └── controller/        #   OrderController, CartController
│   │   ├── common/                # Shared components
│   │   │   ├── dto/               #   ApiResponse
│   │   │   └── exception/         #   BusinessException, GlobalHandler
│   │   └── infrastructure/        # Cross-cutting concerns
│   │       ├── config/            #   SecurityConfig, RedisConfig
│   │       └── security/          #   JwtTokenProvider, JwtFilter
│   ├── src/main/resources/
│   │   ├── db/migration/          #   Flyway V1~V6
│   │   ├── application.yml
│   │   └── application-dev.yml
│   ├── src/test/                  #   JUnit + Mockito tests
│   ├── build.gradle.kts
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/              #   Axios API layer
│   │   ├── store/slices/          #   Redux state
│   │   ├── hooks/
│   │   ├── types/
│   │   └── utils/
│   ├── Dockerfile
│   └── package.json
├── k8s/                           #   Kubernetes manifests
├── docker-compose.yml
├── .github/workflows/ci.yml
└── README.md
```

---

## License

MIT License — see [LICENSE](LICENSE)
