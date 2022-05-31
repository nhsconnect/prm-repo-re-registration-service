locals {
  re_registrations_queue_name     = "${var.environment}-${var.component_name}-re-registrations"
  max_retention_period           = 1209600
  thirty_minute_retention_period = 1800
}

resource "aws_sqs_queue" "re_registrations" {
  name                       = local.re_registrations_queue_name
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id          = data.aws_ssm_parameter.re_registrations_kms_key_id.value
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.re_registrations_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "re_registrations_topic_sub" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = data.aws_ssm_parameter.re_registrations_sns_topic_arn.value
  endpoint             = aws_sqs_queue.re_registrations.arn
}
