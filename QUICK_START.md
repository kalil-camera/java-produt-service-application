# Quick Start Guide

Get the Product Service running in 5 minutes.

## Prerequisites

- Java 25+
- Maven 3.8+
- Docker & Docker Compose
- Git

## 1. Clone & Navigate

```bash
git clone <repository-url>
cd product-service
```

## 2. Start PostgreSQL

```bash
docker-compose up -d
```

This starts PostgreSQL on port 5432 and runs all migrations automatically.

**Verify database is running:**
```bash
docker-compose logs postgres
```

## 3. Build Application

```bash
mvn clean install
```

## 4. Run Application

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Or run the JAR directly:**
```bash
java -jar target/product-service-1.0.0.jar
```

## 5. Verify It's Working

```bash
curl http://localhost:8080/actuator/health
```

**Expected response:**
```json
{
  "status": "UP"
}
```

## Test the API

### Create a Product
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SKU-001",
    "name": "Laptop Pro",
    "description": "High-performance laptop",
    "price": 1299.99,
    "quantity": 50,
    "category": "Electronics"
  }'
```

### Get All Products
```bash
curl http://localhost:8080/api/v1/products
```

### Search Products
```bash
curl "http://localhost:8080/api/v1/products/search?q=laptop"
```

### Get Product by ID
```bash
# Replace {id} with actual product ID from create response
curl http://localhost:8080/api/v1/products/{id}
```

## Run Tests

```bash
mvn test
```

## Stop Services

```bash
docker-compose down
```

## Common Issues

### Port 8080 Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Port 5432 Already in Use
```bash
docker ps  # Find container ID
docker kill <container-id>
```

### Maven Build Fails

Clean and rebuild:
```bash
mvn clean
rm -rf ~/.m2/repository/com/senior/project
mvn install
```

### Database Connection Issues

Check logs:
```bash
docker-compose logs postgres
docker-compose logs product-service
```

Reset database:
```bash
docker-compose down -v
docker-compose up -d
```

## Next Steps

- 📖 Read [README.md](README.md) for full documentation
- 🚀 Check [DEPLOYMENT.md](DEPLOYMENT.md) for AWS deployment
- 📝 Review code structure and architecture
- ✅ Run integration tests: `mvn verify`
- 🐳 Build Docker image: `docker build -t product-service:1.0.0 .`

## Useful Endpoints

- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **API Docs**: http://localhost:8080/api/v1
- **Products**: http://localhost:8080/api/v1/products

## IDE Setup

### VS Code
1. Install Java Extension Pack
2. Open workspace
3. Maven commands from Command Palette

### IntelliJ IDEA
1. Open project folder
2. Select Maven as project type
3. Run → Edit Configurations → Add Application
4. Set Main class: `com.senior.project.ProductServiceApplication`
5. Set Program arguments: `--spring.profiles.active=dev`

### Eclipse
1. File → Import → Existing Maven Projects
2. Select project root
3. Right-click → Run As → Maven Build
4. Goals: `spring-boot:run`

## Database Access

### Connect to PostgreSQL

```bash
# Using psql
psql -h localhost -U postgres -d product_service_dev -W

# Password: postgres (from docker-compose.yml)
```

### Useful SQL Queries

```sql
-- List all products
SELECT * FROM products;

-- Count products by category
SELECT category, COUNT(*) FROM products GROUP BY category;

-- Find low stock items
SELECT sku, name, quantity FROM products WHERE quantity < 10;

-- View audit logs
SELECT * FROM product_audit_log ORDER BY changed_at DESC LIMIT 10;
```

## Performance Testing

### Load Test with Apache Bench
```bash
# Install Apache Bench (ab)
# Create 100 requests with 10 concurrent connections
ab -n 100 -c 10 http://localhost:8080/api/v1/products
```

### Load Test with wrk
```bash
# Install wrk
wrk -t4 -c100 -d30s http://localhost:8080/api/v1/products
```

## Debugging

### Enable Debug Mode

**In IntelliJ/Eclipse:**
- Run → Debug 'ProductServiceApplication'

**In command line:**
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

### View Application Logs
```bash
# Real-time logs
docker-compose logs -f product-service

# Last 100 lines
docker-compose logs --tail=100 product-service

# Formatted
docker-compose logs --timestamps product-service
```

## Environment Variables

Set custom environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/product_service_dev
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export SPRING_PROFILES_ACTIVE=dev

mvn spring-boot:run
```

Or in `application-dev.properties`:
```properties
spring.datasource.url=jdbc:postgresql://your-host:5432/your-db
spring.datasource.username=your-user
spring.datasource.password=your-pass
```

---

**Need help?** Check the full [README.md](README.md) or [DEPLOYMENT.md](DEPLOYMENT.md)
