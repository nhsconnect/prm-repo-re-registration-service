resource "aws_sns_topic" "re_registration_audit_topic" {
  name = "${var.environment}-${var.component_name}-re-registration-audit-sns-topic"
  kms_master_key_id = aws_kms_key.re_registration_audit.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-re-registration-audit-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "re_registration_audit" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.re_registration_audit_topic.arn
  endpoint             = data.aws_sqs_queue.splunk_audit_uploader.arn
}