locals {
  account_id = data.aws_caller_identity.current.account_id
}

data "aws_iam_policy_document" "ecs-assume-role-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"
      identifiers = [
        "ecs-tasks.amazonaws.com"
      ]
    }
  }
}

resource "aws_iam_role" "component-ecs-role" {
  name               = "${var.environment}-${var.component_name}-EcsTaskRole"
  assume_role_policy = data.aws_iam_policy_document.ecs-assume-role-policy.json
  description        = "Role assumed by ${var.component_name} ECS task"

  tags = {
    Environment = var.environment
    CreatedBy   = var.repo_name
  }
}

data "aws_iam_policy_document" "ecr_policy_doc" {
  statement {
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage"
    ]

    resources = [
      "arn:aws:ecr:${var.region}:${local.account_id}:repository/repo/${var.component_name}"
    ]
  }

  statement {
    actions = [
      "ecr:GetAuthorizationToken"
    ]

    resources = [
      "*"
    ]
  }
}

data "aws_iam_policy_document" "logs_policy_doc" {
  statement {
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]

    resources = [
      "arn:aws:logs:${var.region}:${local.account_id}:log-group:/nhs/deductions/${var.environment}-${local.account_id}/${var.component_name}:*"
    ]
  }
}

data "aws_iam_policy_document" "cloudwatch_metrics_policy_doc" {
  statement {
    actions = [
      "cloudwatch:PutMetricData",
      "cloudwatch:GetMetricData"
    ]

    resources = ["*"]
    condition {
      test     = "StringEquals"
      values   = [aws_cloudwatch_metric_alarm.health_metric_failure_alarm.namespace]
      variable = "cloudwatch:namespace"
    }
  }
}

resource "aws_iam_policy" "ecr_policy" {
  name   = "${var.environment}-${var.component_name}-ecr"
  policy = data.aws_iam_policy_document.ecr_policy_doc.json
}

resource "aws_iam_policy" "logs_policy" {
  name   = "${var.environment}-${var.component_name}-logs"
  policy = data.aws_iam_policy_document.logs_policy_doc.json
}

resource "aws_iam_policy" "cloudwatch_metrics_policy" {
  name   = "${var.environment}-${var.component_name}-cloudwatch-metrics"
  policy = data.aws_iam_policy_document.cloudwatch_metrics_policy_doc.json
}

resource "aws_iam_role_policy_attachment" "ecr_policy_attach" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.ecr_policy.arn
}

resource "aws_iam_role_policy_attachment" "logs_policy_attach" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.logs_policy.arn
}

resource "aws_iam_role_policy_attachment" "cloudwatch_metrics_policy_attach" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.cloudwatch_metrics_policy.arn
}

resource "aws_iam_role" "sns_failure_feedback_role" {
  name               = "${var.environment}-${var.component_name}-sns-failure-feedback-role"
  assume_role_policy = data.aws_iam_policy_document.sns_service_assume_role_policy.json
  description        = "Allows logging of SNS delivery failures in ${var.component_name}"

  tags = {
    Environment = var.environment
    CreatedBy   = var.repo_name
  }
}

data "aws_iam_policy_document" "sns_failure_feedback_policy" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:PutMetricFilter",
      "logs:PutRetentionPolicy"
    ]
    resources = [
      "*"
    ]
  }
}

resource "aws_iam_policy" "sns_failure_feedback_policy" {
  name   = "${var.environment}-${var.component_name}-sns-failure-feedback"
  policy = data.aws_iam_policy_document.sns_failure_feedback_policy.json
}

resource "aws_iam_role_policy_attachment" "sns_failure_feedback_policy_attachment" {
  role       = aws_iam_role.sns_failure_feedback_role.name
  policy_arn = aws_iam_policy.sns_failure_feedback_policy.arn
}

data "aws_iam_policy_document" "sns_service_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"
      identifiers = [
        "sns.amazonaws.com"
      ]
    }
  }
}

resource "aws_sqs_queue_policy" "re_registrations_queue_policy" {
  queue_url = aws_sqs_queue.re_registrations.id
  policy    = data.aws_iam_policy_document.re_registrations_sns_topic_access_to_queue.json
}

data "aws_iam_policy_document" "re_registrations_sns_topic_access_to_queue" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.re_registrations.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [data.aws_ssm_parameter.re_registrations_sns_topic_arn.value]
      variable = "aws:SourceArn"
    }
  }
}