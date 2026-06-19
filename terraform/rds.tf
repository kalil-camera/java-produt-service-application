# DB Subnet Group
resource "aws_db_subnet_group" "main" {
  name       = "${var.app_name}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = {
    Name = "${var.app_name}-db-subnet-group"
  }
}

# RDS PostgreSQL Instance
resource "aws_db_instance" "main" {
  identifier              = "${var.app_name}-postgres"
  engine                  = "postgres"
  engine_version          = var.db_engine_version
  instance_class          = var.db_instance_class
  allocated_storage       = var.db_allocated_storage
  storage_type            = "gp3"
  storage_encrypted       = true

  # Database credentials
  db_name  = var.db_name
  username = "postgres"
  password = random_password.db_password.result

  # Networking
  db_subnet_group_name            = aws_db_subnet_group.main.name
  vpc_security_group_ids          = [aws_security_group.rds.id]
  publicly_accessible             = false
  multi_az                         = var.environment != "dev"

  # Backup and maintenance
  backup_retention_period          = var.environment == "prod" ? 30 : 7
  backup_window                    = "03:00-04:00"
  maintenance_window               = "mon:04:00-mon:05:00"
  copy_tags_to_snapshot            = true
  skip_final_snapshot              = var.environment == "dev"
  final_snapshot_identifier        = var.environment != "dev" ? "${var.app_name}-postgres-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}" : null

  # Performance Insights
  performance_insights_enabled          = var.environment == "prod"
  performance_insights_retention_period = var.environment == "prod" ? 31 : null

  # Enhanced Monitoring
  enabled_cloudwatch_logs_exports = ["postgresql"]
  monitoring_interval             = var.environment == "prod" ? 60 : 0
  monitoring_role_arn             = var.environment == "prod" ? aws_iam_role.rds_monitoring[0].arn : null

  # Parameter group
  parameter_group_name = aws_db_parameter_group.main.name

  tags = {
    Name = "${var.app_name}-postgres"
  }

  depends_on = [aws_security_group.rds]
}

# RDS Parameter Group
resource "aws_db_parameter_group" "main" {
  name_prefix = "${var.app_name}-"
  family      = "postgres16"

  parameter {
    name  = "max_connections"
    value = "200"
  }

  parameter {
    name  = "shared_preload_libraries"
    value = "pg_stat_statements"
  }

  tags = {
    Name = "${var.app_name}-db-parameter-group"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# Random password for DB
resource "random_password" "db_password" {
  length  = 32
  special = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# Store DB password in Secrets Manager
resource "aws_secretsmanager_secret" "db_password" {
  name                    = "${var.app_name}-db-password"
  recovery_window_in_days = 7

  tags = {
    Name = "${var.app_name}-db-password"
  }
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id = aws_secretsmanager_secret.db_password.id
  secret_string = jsonencode({
    username = aws_db_instance.main.username
    password = random_password.db_password.result
    engine   = "postgres"
    host     = aws_db_instance.main.address
    port     = aws_db_instance.main.port
    dbname   = aws_db_instance.main.db_name
  })
}

# IAM role for RDS monitoring (only for prod)
resource "aws_iam_role" "rds_monitoring" {
  count = var.environment == "prod" ? 1 : 0
  name  = "${var.app_name}-rds-monitoring-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "monitoring.rds.amazonaws.com"
      }
    }]
  })

  tags = {
    Name = "${var.app_name}-rds-monitoring-role"
  }
}

resource "aws_iam_role_policy_attachment" "rds_monitoring" {
  count      = var.environment == "prod" ? 1 : 0
  role       = aws_iam_role.rds_monitoring[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}

# Outputs for RDS
output "db_endpoint" {
  value       = aws_db_instance.main.endpoint
  description = "RDS endpoint address"
}

output "db_name" {
  value       = aws_db_instance.main.db_name
  description = "Database name"
}

output "db_username" {
  value       = aws_db_instance.main.username
  description = "Database master username"
  sensitive   = true
}

output "db_password_secret_name" {
  value       = aws_secretsmanager_secret.db_password.name
  description = "AWS Secrets Manager secret name for database credentials"
}
