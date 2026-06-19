# Architecture Diagrams & Guides

## 1. Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Client                           │
│                   (HTTP Requests)                            │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│  🔵 PRESENTATION LAYER                                       │
│  ├─ ProductController.java                                   │
│  │  ├─ POST   /api/v1/products                              │
│  │  ├─ GET    /api/v1/products/{id}                         │
│  │  ├─ PUT    /api/v1/products/{id}                         │
│  │  ├─ DELETE /api/v1/products/{id}                         │
│  │  └─ ... (8 endpoints)                                     │
│  └─ GlobalExceptionHandler.java                             │
│     (Centralized error handling)                             │
└────────────────────────────┬────────────────────────────────┘
                             │ DTO: CreateProductRequestDto
                             │
┌────────────────────────────▼────────────────────────────────┐
│  🟢 APPLICATION LAYER                                        │
│  ├─ ProductService.java                                      │
│  │  ├─ createProduct()       @Transactional                 │
│  │  ├─ getProductById()      @Transactional(readOnly=true)  │
│  │  ├─ updateProduct()       @Transactional                 │
│  │  ├─ deleteProduct()       @Transactional                 │
│  │  └─ searchProducts()      @Transactional(readOnly=true)  │
│  └─ ProductMapper.java (MapStruct)                          │
│     (Entity ↔ DTO conversion)                               │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│  🟡 DOMAIN LAYER (Pure Business Logic)                      │
│  ├─ Product.java (JPA Entity)                               │
│  │  ├─ Properties (id, sku, name, price, quantity, etc)    │
│  │  ├─ Business Methods:                                    │
│  │  │  ├─ deductStock()                                     │
│  │  │  ├─ addStock()                                        │
│  │  │  └─ isAvailable()                                     │
│  │  └─ Audit Fields (createdAt, updatedAt, createdBy,      │
│  │     updatedBy)                                            │
│  ├─ ProductStatus (Enum)                                    │
│  │  ├─ ACTIVE                                               │
│  │  ├─ INACTIVE                                             │
│  │  └─ DISCONTINUED                                         │
│  ├─ DomainException.java                                    │
│  └─ ProductNotFoundException.java                           │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│  🔴 INFRASTRUCTURE LAYER                                     │
│  ├─ ProductRepository.java (Spring Data JPA)               │
│  │  ├─ findById()                                           │
│  │  ├─ findBySku()                                          │
│  │  ├─ findAllActive()                                      │
│  │  ├─ findByCategory()                                     │
│  │  ├─ searchProducts()                                     │
│  │  └─ findLowStockProducts()                              │
│  ├─ AuditingConfiguration.java                             │
│  │  └─ AuditorAware<String> Bean                           │
│  └─ Spring Data JPA (Hibernate)                            │
│     └─ ORM Mapping                                          │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│  📊 PERSISTENCE LAYER                                        │
│  ├─ PostgreSQL 16 Database                                  │
│  ├─ Tables:                                                 │
│  │  ├─ products                                             │
│  │  │  ├─ id (UUID)                                         │
│  │  │  ├─ sku (VARCHAR UNIQUE)                              │
│  │  │  ├─ name, description, price, quantity               │
│  │  │  ├─ category, status                                  │
│  │  │  ├─ created_at, updated_at                            │
│  │  │  └─ created_by, updated_by                            │
│  │  └─ product_audit_log                                    │
│  ├─ Indexes (SKU, category, status, created_at)            │
│  └─ Flyway Migrations (version control)                    │
└─────────────────────────────────────────────────────────────┘
```

## 2. Data Flow Diagram

```
┌─────────────────┐
│  HTTP Client    │
└────────┬────────┘
         │
    POST /api/v1/products
    {
      "sku": "SKU-001",
      "name": "Laptop",
      "price": 1299.99,
      "quantity": 50,
      "category": "Electronics"
    }
         │
         ▼
┌─────────────────────────────────────────┐
│  ProductController.createProduct()      │
│  ├─ Validates request (Jakarta)        │
│  └─ Calls ProductService               │
└────────┬────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────────────┐
│  ProductService.createProduct()                  │
│  @Transactional                                  │
│                                                  │
│  1. Check if SKU exists (Repository)            │
│  2. Map DTO to Entity (ProductMapper)           │
│  3. Set audit fields (createdBy, createdAt)     │
│  4. Save to DB (Repository.save)                │
│  5. Map Entity back to DTO                      │
│  6. Return response                             │
└────────┬────────────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────────────┐
│  ProductRepository.findBySku()                   │
│  SELECT * FROM products WHERE sku = ?           │
│  (Named parameter prevents SQL injection)       │
└────────┬────────────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────────────┐
│  ProductRepository.save()                        │
│  INSERT INTO products (...)                      │
│  VALUES (...)                                    │
└────────┬────────────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────────────┐
│  PostgreSQL Database                            │
│  ├─ Validates constraints                       │
│  ├─ Fires triggers (if any)                     │
│  ├─ Updates indexes                             │
│  └─ Logs audit trail                            │
└────────┬────────────────────────────────────────┘
         │
    ✅ Success
         │
         ▼
┌──────────────────────────────────────────────────┐
│  Response: 201 Created                          │
│  {                                               │
│    "id": "550e8400-e29b-41d4-a716-...",         │
│    "sku": "SKU-001",                             │
│    "name": "Laptop",                             │
│    "price": 1299.99,                             │
│    "quantity": 50,                               │
│    "status": "ACTIVE",                           │
│    "created_at": "2024-01-15T10:30:00"          │
│  }                                               │
└──────────────────────────────────────────────────┘
```

## 3. Database Schema Diagram

```
┌─────────────────────────────────────────────────┐
│             PRODUCTS TABLE                       │
├─────────────────────────────────────────────────┤
│ Column         │ Type          │ Constraints     │
├────────────────┼───────────────┼─────────────────┤
│ id             │ UUID          │ PRIMARY KEY     │
│ sku            │ VARCHAR(50)   │ UNIQUE NOT NULL │ ◄─ idx_product_sku
│ name           │ VARCHAR(255)  │ NOT NULL        │
│ description    │ TEXT          │ NULL            │
│ price          │ NUMERIC(10,2) │ NOT NULL > 0    │
│ quantity       │ INTEGER       │ NOT NULL >= 0   │
│ category       │ VARCHAR(50)   │ NOT NULL        │ ◄─ idx_product_category
│ status         │ VARCHAR(20)   │ NOT NULL        │ ◄─ idx_product_status
│ created_at     │ TIMESTAMP     │ NOT NULL        │ ◄─ idx_product_created_at
│ updated_at     │ TIMESTAMP     │ NOT NULL        │
│ created_by     │ VARCHAR(100)  │ NOT NULL        │
│ updated_by     │ VARCHAR(100)  │ NOT NULL        │
└─────────────────────────────────────────────────┘
            ▲
            │ FOREIGN KEY
            │
┌─────────────────────────────────────────────────┐
│      PRODUCT_AUDIT_LOG TABLE                    │
├─────────────────────────────────────────────────┤
│ Column         │ Type          │ Constraints     │
├────────────────┼───────────────┼─────────────────┤
│ id             │ UUID          │ PRIMARY KEY     │
│ product_id     │ UUID          │ FOREIGN KEY ──► products(id)
│ action         │ VARCHAR(20)   │ (CREATE/UPDATE/DELETE)
│ changed_by     │ VARCHAR(100)  │ NOT NULL        │
│ changed_at     │ TIMESTAMP     │ NOT NULL        │
│ old_values     │ JSONB         │ NULL            │
│ new_values     │ JSONB         │ NULL            │
└─────────────────────────────────────────────────┘
```

## 4. AWS Infrastructure Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                        INTERNET                               │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP/HTTPS
                            ▼
            ┌───────────────────────────────┐
            │  Application Load Balancer    │
            │  (Multi-AZ)                   │
            └───┬───────────────────────┬───┘
                │                       │
    ┌───────────▼───────┐    ┌────────▼─────────┐
    │   Public Subnet   │    │  Public Subnet   │
    │   (AZ-1)          │    │  (AZ-2)          │
    │  ┌─────────────┐  │    │  ┌─────────────┐ │
    │  │ NAT Gateway │  │    │  │ NAT Gateway │ │
    │  └─────────────┘  │    │  └─────────────┘ │
    └───────────┬───────┘    └────────┬─────────┘
                │                      │
    ┌───────────▼──────────────────────▼────────┐
    │  VPC (10.0.0.0/16)                         │
    │  ┌──────────────────────────────────────┐ │
    │  │    Private Subnets (ECS Tasks)       │ │
    │  │  ┌─────────────┐   ┌─────────────┐  │ │
    │  │  │ ECS Task 1  │   │ ECS Task 2  │  │ │
    │  │  │ Container   │   │ Container   │  │ │
    │  │  │ Port 8080   │   │ Port 8080   │  │ │
    │  │  └─────────────┘   └─────────────┘  │ │
    │  │         │                 │          │ │
    │  │  ECS Cluster        (Auto-Scaling)   │ │
    │  │  (product-service-cluster)           │ │
    │  └──────────────────────────────────────┘ │
    │  ┌──────────────────────────────────────┐ │
    │  │   Private Subnets (RDS)              │ │
    │  │  ┌────────────────────────────────┐ │ │
    │  │  │  RDS PostgreSQL (Primary)      │ │ │
    │  │  │  ├─ Automated Backups (30d)    │ │ │
    │  │  │  ├─ Multi-AZ Failover          │ │ │
    │  │  │  ├─ Encrypted Storage          │ │ │
    │  │  │  └─ Enhanced Monitoring        │ │ │
    │  │  └────────────────────────────────┘ │ │
    │  └──────────────────────────────────────┘ │
    └──────────────────────────────────────────┘
            │
            ▼
    ┌──────────────────────────┐
    │  AWS Secrets Manager     │
    │  - DB Credentials        │
    │  - API Keys              │
    └──────────────────────────┘
            │
            ▼
    ┌──────────────────────────┐
    │  ECR Repository          │
    │  - Docker Images         │
    │  - Image Scanning        │
    └──────────────────────────┘
            │
            ▼
    ┌──────────────────────────┐
    │  CloudWatch              │
    │  - Application Logs      │
    │  - Metrics               │
    │  - Alarms                │
    └──────────────────────────┘
```

## 5. Request/Response Flow

```
CLIENT REQUEST
     │
     ▼
┌─────────────────────────────────────────┐
│ @PostMapping("/products")               │
│ public ResponseEntity<ProductResponseDto>
│   createProduct(@Valid @RequestBody    │
│                 CreateProductRequestDto)
└────────────┬────────────────────────────┘
             │
     ╔═══════╩═════════╗
     ║ VALIDATION     ║
     ║ ├─ Not blank   ║
     ║ ├─ Not null    ║
     ║ ├─ Valid range ║
     ║ ├─ Size limit  ║
     ║ └─ Pattern     ║
     ╚═══════╤═════════╝
             │
      ┌─────▼──────┐
      │ Valid?     │
      └──┬──────┬──┘
         │      │
       NO│      │YES
         │      ▼
         │  ┌────────────────────────────┐
         │  │ productService.createProd..│
         │  └────────┬───────────────────┘
         │           │
         │      ╔════╩═════════════════╗
         │      ║ @Transactional       ║
         │      ║ ├─ Find existing SKU ║
         │      ║ ├─ Map DTO to Entity ║
         │      ║ ├─ Set audit fields  ║
         │      ║ ├─ Save to DB        ║
         │      ║ └─ Map back to DTO   ║
         │      ╚════╤════════════════╝
         │           │
         │      ┌────▼─────────┐
         │      │ DB Success?  │
         │      └──┬────────┬──┘
         │         │        │
         │      YES│        │NO
         │         │        │
         │         │   ╔════╩═════════╗
         │         │   ║ Rollback     ║
         │         │   ║ Transaction  ║
         │         │   ╚════╤═════════╝
         │         │        │
         │         ▼        │
         │   ┌──────────┐   │
         │   │200 OK    │   │
         │   └──────────┘   │
         │                  │
         ▼                  ▼
     ┌───────────────────────────────┐
     │ Map Exception                 │
     │ ├─ ProductNotFoundException   │
     │ ├─ IllegalArgumentException  │
     │ ├─ Validation Error          │
     │ └─ Generic Exception         │
     └───────┬───────────────────────┘
             │
             ▼
     ┌───────────────────────────────┐
     │ Return Error Response          │
     │ ├─ 400 Bad Request            │
     │ ├─ 404 Not Found              │
     │ ├─ 500 Internal Server Error  │
     │ └─ Timestamp & Message        │
     └───────────────────────────────┘
```

## 6. Transaction Flow

```
                 ┌─────────────────────────┐
                 │ @Transactional          │
                 │ (readOnly=false,        │
                 │  propagation=REQUIRED)  │
                 └────────────┬────────────┘
                              │
              ┌───────────────┴───────────────┐
              │                               │
              ▼                               ▼
      ┌──────────────────┐          ┌──────────────────┐
      │ BEGIN            │          │ Nested           │
      │ TRANSACTION      │          │ Transaction?     │
      └────────┬─────────┘          └────────┬─────────┘
               │                            │
               ▼                            ▼
      ┌──────────────────────────────────────────────┐
      │ Execute Business Logic                       │
      │ ├─ Read operations (SELECT)                  │
      │ ├─ Write operations (INSERT/UPDATE/DELETE)  │
      │ ├─ Call other @Transactional services      │
      │ └─ Interact with database                   │
      └────────┬──────────┬──────────┬───────────────┘
               │          │          │
          ┌────▼──┐ ┌─────▼──┐ ┌────▼──────┐
          │Success│ │Warning │ │ Exception │
          └───┬──┘ └────┬────┘ └────┬──────┘
              │         │            │
              ▼         ▼            ▼
        ┌────────┐ ┌────────┐ ┌───────────────┐
        │ COMMIT │ │ COMMIT │ │ ROLLBACK      │
        │        │ │ (Log)  │ │ (All changes  │
        │ All    │ │        │ │  reverted)    │
        │Changes │ │        │ │               │
        │Saved   │ │        │ │ Return error  │
        └────────┘ └────────┘ └───────────────┘
```

## 7. Error Handling Flow

```
┌──────────────────────┐
│ Request Arrives      │
└──────────┬───────────┘
           │
           ▼
    ┌─────────────────┐
    │ Input Validation│
    └────┬───────┬────┘
         │       │
     Valid       Invalid
         │       │
         │       ▼
         │   ┌─────────────────────────────┐
         │   │ MethodArgumentNotValidException
         │   └─────────┬───────────────────┘
         │             │
         │             ▼
         │         ┌──────────────┐
         │         │ 400 Bad Req. │
         │         └──────────────┘
         │
         ▼
    ┌────────────────┐
    │ Service Logic  │
    └────┬───────┬───┘
         │       │
      Success    Error
         │       │
         │       ▼
         │   ┌────────────────────────┐
         │   │ BusinessException      │
         │   │ ├─ ProductNotFound     │
         │   │ ├─ InvalidStock        │
         │   │ └─ DuplicateSKU        │
         │   └────────┬────────────────┘
         │            │
         │            ▼
         │        ┌──────────────┐
         │        │ 404/400      │
         │        └──────────────┘
         │
         ▼
    ┌────────────────────┐
    │ GlobalExceptionHandler
    └────┬────────┬──────┘
         │        │
    Exception    Success
         │        │
         ▼        ▼
    ┌─────────────────────────┐
    │ ErrorResponse {         │
    │   timestamp,            │
    │   status,               │
    │   error,                │
    │   message,              │
    │   fieldErrors (opt)     │
    │ }                       │
    └─────────────────────────┘
```

---

These diagrams show the complete flow of the application from request to response, including all the layers, database design, AWS infrastructure, and error handling mechanisms.
