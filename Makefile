.PHONY: help build test run clean docker logs stop deploy-dev deploy-prod

# Colors for output
BLUE = \033[0;34m
GREEN = \033[0;32m
NC = \033[0m # No Color

help:
	@echo "$(BLUE)Product Service - Development Commands$(NC)"
	@echo ""
	@echo "$(GREEN)Setup$(NC)"
	@echo "  make install        - Install dependencies and build project"
	@echo "  make build          - Build the application"
	@echo ""
	@echo "$(GREEN)Development$(NC)"
	@echo "  make run            - Run the application locally"
	@echo "  make test           - Run unit tests"
	@echo "  make test-integration - Run integration tests"
	@echo "  make clean          - Clean build artifacts"
	@echo ""
	@echo "$(GREEN)Docker$(NC)"
	@echo "  make docker-build   - Build Docker image"
	@echo "  make docker-run     - Run application with Docker"
	@echo "  make docker-compose - Start services with Docker Compose"
	@echo "  make docker-stop    - Stop Docker Compose services"
	@echo "  make docker-logs    - View Docker logs"
	@echo ""
	@echo "$(GREEN)Database$(NC)"
	@echo "  make db-start       - Start PostgreSQL"
	@echo "  make db-stop        - Stop PostgreSQL"
	@echo "  make db-logs        - View database logs"
	@echo "  make db-reset       - Reset database (WARNING: deletes all data)"
	@echo ""
	@echo "$(GREEN)Terraform$(NC)"
	@echo "  make tf-init        - Initialize Terraform"
	@echo "  make tf-plan-dev    - Plan development infrastructure"
	@echo "  make tf-plan-prod   - Plan production infrastructure"
	@echo "  make tf-apply-dev   - Deploy development infrastructure"
	@echo "  make tf-apply-prod  - Deploy production infrastructure"
	@echo ""
	@echo "$(GREEN)Code Quality$(NC)"
	@echo "  make format         - Format code"
	@echo "  make lint           - Run linter"
	@echo ""

# Build targets
install:
	@echo "$(GREEN)Installing dependencies...$(NC)"
	mvn clean install

build:
	@echo "$(GREEN)Building project...$(NC)"
	mvn clean package

# Run targets
run:
	@echo "$(GREEN)Starting application...$(NC)"
	mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

run-prod:
	@echo "$(GREEN)Starting application (prod mode)...$(NC)"
	mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Test targets
test:
	@echo "$(GREEN)Running unit tests...$(NC)"
	mvn test

test-integration:
	@echo "$(GREEN)Running integration tests...$(NC)"
	mvn verify

test-coverage:
	@echo "$(GREEN)Running tests with coverage...$(NC)"
	mvn clean test jacoco:report
	@echo "Coverage report: target/site/jacoco/index.html"

# Clean targets
clean:
	@echo "$(GREEN)Cleaning build artifacts...$(NC)"
	mvn clean
	rm -rf target/
	rm -rf logs/

clean-cache:
	@echo "$(GREEN)Clearing Maven cache...$(NC)"
	rm -rf ~/.m2/repository/com/senior/project

# Docker targets
docker-build:
	@echo "$(GREEN)Building Docker image...$(NC)"
	mvn clean package
	docker build -t product-service:1.0.0 .

docker-run:
	@echo "$(GREEN)Running application with Docker...$(NC)"
	docker run \
		--name product-service \
		-p 8080:8080 \
		-e SPRING_PROFILES_ACTIVE=dev \
		product-service:1.0.0

docker-compose:
	@echo "$(GREEN)Starting services with Docker Compose...$(NC)"
	docker-compose up -d
	@echo "Waiting for services to start..."
	sleep 5
	@echo "Services started. Checking health..."
	curl http://localhost:8080/actuator/health 2>/dev/null | jq . || echo "Service not ready yet"

docker-stop:
	@echo "$(GREEN)Stopping Docker Compose services...$(NC)"
	docker-compose down

docker-logs:
	@echo "$(GREEN)Showing Docker logs...$(NC)"
	docker-compose logs -f

docker-remove:
	@echo "$(GREEN)Removing Docker containers and images...$(NC)"
	docker-compose down -v
	docker rmi product-service:1.0.0

# Database targets
db-start:
	@echo "$(GREEN)Starting PostgreSQL...$(NC)"
	docker-compose up -d postgres

db-stop:
	@echo "$(GREEN)Stopping PostgreSQL...$(NC)"
	docker-compose down

db-logs:
	@echo "$(GREEN)Showing database logs...$(NC)"
	docker-compose logs -f postgres

db-reset:
	@echo "$(BLUE)WARNING: This will delete all database data$(NC)"
	@read -p "Are you sure? (yes/no) " confirm; \
	if [ "$$confirm" = "yes" ]; then \
		docker-compose down -v; \
		docker-compose up -d postgres; \
		sleep 5; \
		echo "$(GREEN)Database reset complete$(NC)"; \
	else \
		echo "Cancelled"; \
	fi

db-connect:
	@echo "$(GREEN)Connecting to PostgreSQL...$(NC)"
	@docker-compose exec postgres psql -U postgres -d product_service_dev

# Terraform targets
tf-init:
	@echo "$(GREEN)Initializing Terraform...$(NC)"
	cd terraform && terraform init

tf-plan-dev:
	@echo "$(GREEN)Planning development infrastructure...$(NC)"
	cd terraform && terraform plan -var-file="dev.tfvars"

tf-plan-prod:
	@echo "$(GREEN)Planning production infrastructure...$(NC)"
	cd terraform && terraform plan -var-file="prod.tfvars"

tf-apply-dev:
	@echo "$(GREEN)Applying development infrastructure...$(NC)"
	cd terraform && terraform apply -var-file="dev.tfvars"

tf-apply-prod:
	@echo "$(BLUE)WARNING: Deploying to PRODUCTION$(NC)"
	@read -p "Are you sure? (yes/no) " confirm; \
	if [ "$$confirm" = "yes" ]; then \
		cd terraform && terraform apply -var-file="prod.tfvars"; \
	else \
		echo "Cancelled"; \
	fi

tf-destroy-dev:
	@echo "$(GREEN)Destroying development infrastructure...$(NC)"
	cd terraform && terraform destroy -var-file="dev.tfvars"

tf-destroy-prod:
	@echo "$(BLUE)WARNING: DESTROYING PRODUCTION INFRASTRUCTURE$(NC)"
	@read -p "Type 'yes' to confirm: " confirm; \
	if [ "$$confirm" = "yes" ]; then \
		cd terraform && terraform destroy -var-file="prod.tfvars"; \
	else \
		echo "Cancelled"; \
	fi

tf-output:
	@echo "$(GREEN)Terraform outputs:$(NC)"
	cd terraform && terraform output

# Code quality targets
format:
	@echo "$(GREEN)Formatting code...$(NC)"
	mvn spotless:apply

lint:
	@echo "$(GREEN)Running code analysis...$(NC)"
	mvn checkstyle:check

check:
	@echo "$(GREEN)Running full code check...$(NC)"
	mvn clean verify spotless:check checkstyle:check

# Documentation targets
docs:
	@echo "$(GREEN)Available documentation:$(NC)"
	@echo "  - README.md       - Main documentation"
	@echo "  - QUICK_START.md  - Get started quickly"
	@echo "  - DEPLOYMENT.md   - Deployment guide"

# API targets
api-health:
	@echo "$(GREEN)Checking application health...$(NC)"
	curl http://localhost:8080/actuator/health | jq .

api-metrics:
	@echo "$(GREEN)Application metrics:$(NC)"
	curl http://localhost:8080/actuator/metrics | jq .

api-products:
	@echo "$(GREEN)Fetching products...$(NC)"
	curl http://localhost:8080/api/v1/products | jq .

api-sample:
	@echo "$(GREEN)Creating sample product...$(NC)"
	curl -X POST http://localhost:8080/api/v1/products \
		-H "Content-Type: application/json" \
		-d '{ \
			"sku": "SKU-SAMPLE", \
			"name": "Sample Product", \
			"description": "This is a sample product", \
			"price": 99.99, \
			"quantity": 100, \
			"category": "Sample" \
		}' | jq .

# Development workflow
dev-setup: install docker-compose
	@echo "$(GREEN)Development environment setup complete!$(NC)"
	@echo "Run 'make run' to start the application"

start: docker-compose run
	@echo "$(GREEN)Application started!$(NC)"

stop: docker-stop
	@echo "$(GREEN)Application stopped!$(NC)"

restart: docker-stop docker-compose run
	@echo "$(GREEN)Application restarted!$(NC)"

status:
	@echo "$(GREEN)Service Status:$(NC)"
	@docker-compose ps
	@echo ""
	@echo "$(GREEN)Application Health:$(NC)"
	@curl -s http://localhost:8080/actuator/health | jq . || echo "Application not running"
