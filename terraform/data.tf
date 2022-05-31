data "aws_caller_identity" "current" {}

data "aws_sns_topic" "alarm_notifications" {
  name = "${var.environment}-alarm-notifications-sns-topic"
}

data "aws_ssm_parameter" "deductions_private_vpc_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/private-vpc-id"
}

data "aws_ssm_parameter" "deductions_private_private_subnets" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/deductions-private-private-subnets"
}

data "aws_ssm_parameter" "re_registrations_kms_key_id" {
  name = "/repo/${var.environment}/output/prm-deductions-nems-event-processor/re-registrations-kms-key-id"
}

data "aws_ssm_parameter" "re_registrations_sns_topic_arn" {
  name = "/repo/${var.environment}/output/nems-event-processor/re-registrations-sns-topic-arn"
}