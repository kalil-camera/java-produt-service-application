# Deployment Guide

This guide covers deploying the Product Service to AWS using Terraform and Docker.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Building the Application](#building-the-application)
3. [Creating Docker Image](#creating-docker-image)
4. [Setting Up AWS](#setting-up-aws)
5. [Deploying with Terraform](#deploying-with-terraform)
6. [Pushing Images to ECR](#pushing-images-to-ecr)
7. [Updating Deployments](#updating-deployments)
8. [Monitoring & Troubleshooting](#monitoring--troubleshooting)

## Prerequisites

### Tools Required

- Java 25+
- Maven 3.8+
- Docker & Docker Compose
- Terraform 1.5+
- AWS CLI v2
- Git

### AWS Account Setup

1. Create an AWS account or use existing one
2. Install and configure AWS CLI:
```bash
aws configure
# Enter: Access Key ID, Secret Access Key, Region (us-east-1), Output Format (json)
```

3. Verify AWS credentials:
```bash
aws sts get-caller-identity
```

## Building the Application

### Step 1: Clone Repository
```bash
git clone <repository-url>
cd product-service
```

### Step 2: Build with Maven
```bash
mvn clean package
```

This creates:
- `target/product-service-1.0.0.jar`
- Runs all tests
- Generates dependencies

### Step 3: Verify Build
```bash
java -jar target/product-service-1.0.0.jar --version
```

## Creating Docker Image

### Step 1: Build Image Locally
```bash
docker build -t product-service:1.0.0 .
```

### Step 2: Test Image Locally
```bash
# Start PostgreSQL
docker-compose up -d postgres

# Run container
docker run \
  --name product-service-test \
  --network=host \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/product_service_dev \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e SPRING_PROFILES_ACTIVE=dev \
  product-service:1.0.0

# Test application
curl http://localhost:8080/actuator/health

# Stop container
docker stop product-service-test
docker rm product-service-test
```

### Step 3: Verify Image
```bash
docker images | grep product-service
docker inspect product-service:1.0.0
```

## Setting Up AWS

### Step 1: Create S3 Bucket for Terraform State (Optional but Recommended)
```bash
aws s3api create-bucket \
  --bucket product-service-terraform-state-$(date +%s) \
  --region us-east-1

aws s3api put-bucket-versioning \
  --bucket product-service-terraform-state-<timestamp> \
  --versioning-configuration Status=Enabled

aws s3api put-bucket-encryption \
  --bucket product-service-terraform-state-<timestamp> \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'
```

### Step 2: Enable Remote State (Uncomment in terraform/main.tf)
```hcl
backend "s3" {
  bucket         = "product-service-terraform-state-<timestamp>"
  key            = "prod/terraform.tfstate"
  region         = "us-east-1"
  encrypt        = true
  dynamodb_table = "terraform-locks"
}
```

### Step 3: Create DynamoDB Table for Locks
```bash
aws dynamodb create-table \
  --table-name terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --region us-east-1
```

## Deploying with Terraform

### Step 1: Initialize Terraform
```bash
cd terraform
terraform init
```

### Step 2: Plan Deployment

**Development**
```bash
terraform plan -var-file="dev.tfvars" -out="dev.tfplan"
```

**Production**
```bash
terraform plan -var-file="prod.tfvars" -out="prod.tfplan"
```

Review the output carefully for any changes.

### Step 3: Apply Terraform Configuration

**Development**
```bash
terraform apply "dev.tfplan"
```

**Production**
```bash
terraform apply "prod.tfplan"
```

This will create:
- VPC with public/private subnets
- RDS PostgreSQL database
- ECR repository
- ECS cluster and service
- Application Load Balancer
- CloudWatch logs
- Security groups and IAM roles
- Auto-scaling policies

### Step 4: Get Outputs
```bash
terraform output
# Save the ECR repository URL and ALB DNS name
```

## Pushing Images to ECR

### Step 1: Get ECR Repository URL
```bash
ECR_REPO=$(terraform output -raw ecr_repository_url)
echo $ECR_REPO
```

### Step 2: Authenticate with ECR
```bash
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_REPO
```

### Step 3: Tag Image
```bash
docker tag product-service:1.0.0 $ECR_REPO:1.0.0
docker tag product-service:1.0.0 $ECR_REPO:latest
```

### Step 4: Push Image
```bash
docker push $ECR_REPO:1.0.0
docker push $ECR_REPO:latest
```

### Step 5: Verify Image in ECR
```bash
aws ecr describe-images --repository-name product-service
```

## Updating Deployments

### Method 1: Using AWS Console
1. Go to ECS → Clusters → product-service-cluster
2. Select product-service-service
3. Click "Update Service"
4. Check "Force new deployment"
5. Update

### Method 2: Using AWS CLI
```bash
aws ecs update-service \
  --cluster product-service-cluster \
  --service product-service-service \
  --force-new-deployment \
  --region us-east-1
```

### Method 3: Using Terraform
```bash
# Push new image first
docker tag product-service:1.1.0 $ECR_REPO:1.1.0
docker push $ECR_REPO:1.1.0

# Update app version variable
terraform apply -var-file="prod.tfvars" -var="app_version=1.1.0"
```

## Monitoring & Troubleshooting

### View Application Logs
```bash
# Get log group name
LOG_GROUP=$(terraform output -raw cloudwatch_log_group)

# View recent logs
aws logs tail $LOG_GROUP --follow
```

### Check ECS Service Status
```bash
aws ecs describe-services \
  --cluster product-service-cluster \
  --services product-service-service \
  --query 'services[0].[status,runningCount,desiredCount]'
```

### Check Task Status
```bash
aws ecs list-tasks \
  --cluster product-service-cluster \
  --service-name product-service-service

# Get detailed task info
aws ecs describe-tasks \
  --cluster product-service-cluster \
  --tasks <task-arn>
```

### View Health Check Results
```bash
ALB_DNS=$(terraform output -raw alb_dns_name)
curl http://$ALB_DNS/actuator/health
```

### Check RDS Connection
```bash
# Get RDS endpoint
DB_ENDPOINT=$(terraform output -raw db_endpoint)
echo $DB_ENDPOINT

# Test connectivity
psql -h <host-from-endpoint> -U postgres -d product_service -c "SELECT NOW();"
```

### Check Database Credentials
```bash
SECRET_NAME=$(terraform output -raw secrets_manager_secret_name)
aws secretsmanager get-secret-value --secret-id $SECRET_NAME
```

### View Resource Metrics
```bash
# ECS Service metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/ECS \
  --metric-name CPUUtilization \
  --dimensions Name=ServiceName,Value=product-service-service \
               Name=ClusterName,Value=product-service-cluster \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Average
```

### Troubleshoot Common Issues

#### Tasks Failing to Start

1. **Check logs**
```bash
aws logs describe-log-streams --log-group-name /ecs/product-service
aws logs get-log-events --log-group-name /ecs/product-service --log-stream-name ecs/<stream>
```

2. **Common issues**
   - Database credentials wrong → Check Secrets Manager
   - Port already in use → Check security groups
   - Image not found → Verify ECR push success
   - Out of memory → Increase container_memory in tfvars

#### High CPU Usage

1. **Scale tasks up**
```bash
aws ecs update-service \
  --cluster product-service-cluster \
  --service product-service-service \
  --desired-count 5
```

2. **Check application logs for errors**

#### Database Connection Issues

1. **Verify security group allows access**
```bash
aws ec2 describe-security-groups \
  --group-ids <rds-sg-id> \
  --query 'SecurityGroups[0].IpPermissions'
```

2. **Check RDS status**
```bash
aws rds describe-db-instances \
  --db-instance-identifier product-service-postgres \
  --query 'DBInstances[0].[DBInstanceStatus,Endpoint]'
```

## Destroying Infrastructure

⚠️ **WARNING**: This will delete all resources

```bash
cd terraform
terraform destroy -var-file="prod.tfvars"
```

To skip destruction of specific resources:

```bash
terraform destroy -var-file="prod.tfvars" \
  -target=aws_db_instance.main \
  -target=aws_rds_cluster_snapshot.snapshot
```

## Production Checklist

Before deploying to production:

- [ ] Set `environment = "prod"` in tfvars
- [ ] Updated `app_version` in variables
- [ ] Tested all endpoints locally
- [ ] Reviewed security groups
- [ ] Enabled Multi-AZ for RDS
- [ ] Verified backup retention (30 days)
- [ ] Enabled CloudWatch alarms
- [ ] Set up auto-scaling policies
- [ ] Configured proper logging levels
- [ ] Enabled HTTPS with valid certificate
- [ ] Set up monitoring and alerting
- [ ] Documented runbooks for operations team
- [ ] Performed load testing
- [ ] Set up disaster recovery plan

## Support & Maintenance

### Daily Operations

1. Monitor CloudWatch dashboards
2. Review application logs
3. Check database replication lag
4. Verify backup jobs completed

### Weekly Maintenance

1. Review and rotate access keys
2. Update security patches
3. Review cost optimization opportunities
4. Validate disaster recovery procedures

### Monthly Reviews

1. Capacity planning
2. Performance optimization
3. Security audit
4. Disaster recovery drill

---

For additional help, refer to AWS documentation:
- [ECS Getting Started](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/getting-started-ecs-ec2.html)
- [RDS User Guide](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Welcome.html)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
