variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be one of: dev, staging, prod"
  }
}

variable "app_name" {
  description = "Application name"
  type        = string
  default     = "product-service"
}

variable "app_version" {
  description = "Application version"
  type        = string
  default     = "1.0.0"
}

# RDS Variables
variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 20
}

variable "db_engine_version" {
  description = "PostgreSQL engine version"
  type        = string
  default     = "16.1"
}

variable "db_name" {
  description = "Initial database name"
  type        = string
  default     = "product_service"
  sensitive   = false
}

# ECS Variables
variable "container_port" {
  description = "Container port for the application"
  type        = number
  default     = 8080
}

variable "container_cpu" {
  description = "CPU units for ECS task"
  type        = number
  default     = 512
}

variable "container_memory" {
  description = "Memory in MB for ECS task"
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Number of ECS tasks to run"
  type        = number
  default     = 2
}

variable "log_retention_in_days" {
  description = "CloudWatch logs retention in days"
  type        = number
  default     = 7
}

# VPC Variables
variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "tags" {
  description = "Additional tags to apply to resources"
  type        = map(string)
  default     = {}
}
