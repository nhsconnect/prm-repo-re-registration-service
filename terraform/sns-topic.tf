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