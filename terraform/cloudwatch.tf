locals {
  re_registration_service_metric_namespace = "ReRegistrationService"
  error_logs_metric_name              = "ErrorCountInLogs"
}

resource "aws_cloudwatch_log_group" "log_group" {
  name = "/nhs/deductions/${var.environment}-${data.aws_caller_identity.current.account_id}/${var.component_name}"

  tags = {
    Environment = var.environment
    CreatedBy= var.repo_name
  }
}

resource "aws_cloudwatch_metric_alarm" "health_metric_failure_alarm" {
  alarm_name                = "${var.environment}-${var.component_name}-health-metric-failure"
  comparison_operator       = "LessThanThreshold"
  threshold                 = var.service_desired_count
  evaluation_periods        = "3"
  metric_name               = "Health"
  namespace                 = local.re_registration_service_metric_namespace
  alarm_description         = "Alarm to flag failed health checks"
  statistic                 = "Maximum"
  treat_missing_data        = "breaching"
  datapoints_to_alarm       = var.service_desired_count
  period                    = "60"
  dimensions = {
    "Environment" = var.environment
  }
  alarm_actions             = [data.aws_sns_topic.alarm_notifications.arn]
}
resource "aws_cloudwatch_log_metric_filter" "log_metric_filter" {
  name           = "${var.environment}-${var.component_name}-error-logs"
  pattern        = "{ $.level = \"ERROR\" }"
  log_group_name = aws_cloudwatch_log_group.log_group.name

  metric_transformation {
    name          = local.error_logs_metric_name
    namespace     = local.re_registration_service_metric_namespace
    value         = 1
    default_value = 0
  }
}