# Application Load Balancer
resource "aws_lb" "main" {
  name               = "${var.app_name}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id

  enable_deletion_protection = var.environment == "prod"
  enable_http2               = true
  enable_cross_zone_load_balancing = true

  tags = {
    Name = "${var.app_name}-alb"
  }
}

# Target Group
resource "aws_lb_target_group" "app" {
  name        = "${var.app_name}-tg"
  port        = var.container_port
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    matcher             = "200"
  }

  tags = {
    Name = "${var.app_name}-tg"
  }
}

# HTTP Listener (redirect to HTTPS in prod)
resource "aws_lb_listener" "app" {
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

# HTTPS Listener (only in prod with certificate)
resource "aws_lb_listener" "app_https" {
  count             = var.environment == "prod" ? 1 : 0
  load_balancer_arn = aws_lb.main.arn
  port              = "443"
  protocol          = "HTTPS"
  certificate_arn   = aws_acm_certificate.main[0].arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }

  depends_on = [aws_acm_certificate_validation.main]
}

# HTTP to HTTPS Redirect (only in prod)
resource "aws_lb_listener" "app_http_redirect" {
  count             = var.environment == "prod" ? 1 : 0
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# ACM Certificate (only in prod)
resource "aws_acm_certificate" "main" {
  count             = var.environment == "prod" ? 1 : 0
  domain_name       = "api.example.com"  # Change to your domain
  validation_method = "DNS"

  tags = {
    Name = "${var.app_name}-certificate"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# Certificate Validation
resource "aws_acm_certificate_validation" "main" {
  count           = var.environment == "prod" ? 1 : 0
  certificate_arn = aws_acm_certificate.main[0].arn

  timeouts {
    create = "5m"
  }
}

# ECR Repository
resource "aws_ecr_repository" "main" {
  name                 = var.app_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = var.environment == "prod"
  }

  tags = {
    Name = "${var.app_name}-ecr"
  }
}

# ECR Repository Policy
resource "aws_ecr_repository_policy" "main" {
  repository = aws_ecr_repository.main.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        AWS = aws_iam_role.ecs_task_execution_role.arn
      }
      Action = [
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:BatchCheckLayerAvailability"
      ]
    }]
  })
}

# Outputs for ALB and ECR
output "alb_dns_name" {
  value       = aws_lb.main.dns_name
  description = "DNS name of the load balancer"
}

output "ecr_repository_url" {
  value       = aws_ecr_repository.main.repository_url
  description = "ECR repository URL"
}

output "ecr_repository_name" {
  value       = aws_ecr_repository.main.name
  description = "ECR repository name"
}

output "target_group_arn" {
  value       = aws_lb_target_group.app.arn
  description = "Target group ARN"
}
