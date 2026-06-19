output "environment" {
  value       = var.environment
  description = "Environment name"
}

output "application_url" {
  value       = var.environment == "prod" ? "https://${aws_lb.main.dns_name}" : "http://${aws_lb.main.dns_name}"
  description = "Application URL"
}

output "database_endpoint" {
  value       = aws_db_instance.main.endpoint
  description = "RDS database endpoint"
}

output "ecr_repository_url" {
  value       = aws_ecr_repository.main.repository_url
  description = "ECR repository URL for pushing Docker images"
}

output "ecs_cluster_name" {
  value       = aws_ecs_cluster.main.name
  description = "ECS cluster name"
}

output "ecs_service_name" {
  value       = aws_ecs_service.main.name
  description = "ECS service name"
}

output "cloudwatch_log_group" {
  value       = aws_cloudwatch_log_group.ecs.name
  description = "CloudWatch log group name"
}

output "vpc_id" {
  value       = aws_vpc.main.id
  description = "VPC ID"
}

output "secrets_manager_secret_name" {
  value       = aws_secretsmanager_secret.db_password.name
  description = "AWS Secrets Manager secret name for database credentials"
}
