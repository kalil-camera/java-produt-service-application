# Product Service

A Spring Boot 3.3 application with Java 25, demonstrating enterprise-grade development practices, scalable architecture, and infrastructure as code using Terraform.

## Project Overview

This project showcases knowledge across:

- **Java 25** - Modern Java features and best practices
- **Spring Boot 3.3** - Layered architecture and dependency injection
- **PostgreSQL 16** - Database design and optimization
- **Terraform** - Infrastructure as Code for AWS
- **Docker** - Containerization and orchestration
- **AWS Services** - ECS, RDS, ALB, ECR, CloudWatch
- **Design Patterns** - SOLID principles, Domain-Driven Design
- **Testing** - Unit and integration tests
- **Security** - Secrets management, security groups, IAM roles

## 📋 Table of Contents

1. [Project Structure](#project-structure)
2. [Architecture](#architecture)
3. [Getting Started](#getting-started)
4. [Development](#development)
5. [Testing](#testing)
6. [Docker Deployment](#docker-deployment)
7. [Terraform Infrastructure](#terraform-infrastructure)
8. [API Documentation](#api-documentation)
9. [Database Migrations](#database-migrations)
10. [Best Practices Demonstrated](#best-practices-demonstrated)

## 📁 Project Structure

```
product-service/
├── src/
│   ├── main/
│   │   ├── java/com/senior/project/
│   │   │   ├── ProductServiceApplication.java      # Main entry point
│   │   │   ├── application/
│   │   │   │   ├── dto/                            # Data Transfer Objects
│   │   │   │   ├── mapper/                         # MapStruct mappers
│   │   │   │   └── service/                        # Business logic
│   │   │   ├── domain/
│   │   │   │   ├── entity/                         # JPA entities
│   │   │   │   └── exception/                      # Domain exceptions
│   │   │   ├── infrastructure/
│   │   │   │   ├── config/                         # Spring configurations
│   │   │   │   └── repository/                     # Data access layer
│   │   │   └── presentation/
│   │   │       ├── controller/                     # REST endpoints
│   │   │       └── exception/                      # Global exception handler
│   │   └── resources/
│   │       ├── application.properties              # Base configuration
│   │       ├── application-dev.properties          # Dev environment
│   │       ├── application-prod.properties         # Prod environment
│   │       └── db/migration/                       # Flyway migrations
│   └── test/
│       └── java/com/senior/project/
│           └── application/service/                # Service tests
├── terraform/
│   ├── main.tf                                     # Provider configuration
│   ├── variables.tf                                # Input variables
│   ├── vpc.tf                                      # Network configuration
│   ├── rds.tf                                      # Database setup
│   ├── ecs.tf                                      # Container orchestration
│   ├── alb.tf                                      # Load balancer
│   ├── outputs.tf                                  # Output values
│   ├── dev.tfvars                                  # Dev environment vars
│   └── prod.tfvars                                 # Prod environment vars
├── pom.xml                                         # Maven configuration
├── Dockerfile                                      # Docker image
├── docker-compose.yml                              # Local development
└── README.md                                       # This file
```

## 🏗️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│  Presentation Layer (REST API)      │ ProductController
├─────────────────────────────────────┤
│  Application Layer (Business Logic) │ ProductService
├─────────────────────────────────────┤
│  Domain Layer (Entities & Rules)    │ Product, ProductStatus
├─────────────────────────────────────┤
│  Infrastructure Layer (Persistence) │ ProductRepository
└─────────────────────────────────────┘
```

### Key Design Patterns

1. **Repository Pattern** - Data access abstraction
2. **Service Pattern** - Business logic encapsulation
3. **DTO Pattern** - API contract separation from domain
4. **Mapper Pattern** - Object transformation using MapStruct
5. **Global Exception Handler** - Centralized error management
6. **Dependency Injection** - Spring IoC container
7. **Transaction Management** - @Transactional for ACID compliance

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 16 (or use Docker)
- Terraform 1.5+
- AWS CLI (for cloud deployment)

### Local Development Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd product-service
```

2. **Start PostgreSQL using Docker Compose**
```bash
docker-compose up -d
```

This will:
- Start PostgreSQL 16 on port 5432
- Create the `product_service_dev` database
- Run Flyway migrations automatically
- Insert sample data

3. **Build the application**
```bash
mvn clean install
```

4. **Run the application**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

The application will start on `http://localhost:8080`

5. **Verify the application is running**
```bash
curl http://localhost:8080/actuator/health
```

## 🛠️ Development

### Running Tests

**Unit Tests**
```bash
mvn test
```

**Integration Tests** (requires running PostgreSQL)
```bash
mvn verify
```

## 🐳 Docker Deployment

### Build Docker Image

```bash
mvn clean package
docker build -t product-service:1.0.0 .
```

### Run with Docker Compose (Development)

```bash
docker-compose up -d
```

## 🏗️ Terraform Infrastructure

### Deploy Infrastructure

**Development Environment**
```bash
cd terraform
terraform init
terraform plan -var-file="dev.tfvars"
terraform apply -var-file="dev.tfvars"
```

**Production Environment**
```bash
terraform plan -var-file="prod.tfvars"
terraform apply -var-file="prod.tfvars"
```

## 📡 API Endpoints

### Products API

- `POST /api/v1/products` - Create product
- `GET /api/v1/products` - List all products (paginated)
- `GET /api/v1/products/{id}` - Get product by ID
- `GET /api/v1/products/sku/{sku}` - Get product by SKU
- `GET /api/v1/products/search?q={term}` - Search products
- `GET /api/v1/products/category/{category}` - Get by category
- `GET /api/v1/products/low-stock` - Get low stock products
- `PUT /api/v1/products/{id}` - Update product
- `DELETE /api/v1/products/{id}` - Delete product


✅ Clean, layered architecture  
✅ SOLID principles and DDD  
✅ Dependency injection  
✅ Repository pattern  
✅ Service layer with business logic  
✅ DTOs for API contracts  
✅ Input validation  
✅ Global exception handling  
✅ Logging and monitoring  
✅ Transaction management  
✅ Database migrations (Flyway)  
✅ Unit and integration tests  
✅ Docker containerization  
✅ Infrastructure as Code (Terraform)  
✅ AWS best practices (VPC, Security Groups, IAM, RDS, ECS, ALB)  
✅ Auto-scaling policies  
✅ Secrets management  
✅ Health checks and monitoring  

---

**Version**: 1.0.0  
**Java**: 25  
**Spring Boot**: 3.3.0  
**PostgreSQL**: 16