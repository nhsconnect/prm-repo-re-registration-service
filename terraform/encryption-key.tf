resource "aws_kms_key" "re_registration_audit" {
  description = "Custom KMS Key to enable server side encryption for SNS and SQS"
  policy      = data.aws_iam_policy_document.re_registration_audit_kms_key_policy_doc.json
  enable_key_rotation = true

  tags = {
    Name        = "${var.environment}-re-registration-audit-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "re_registration_audit" {
  name          = "alias/re-registration-audit-encryption-kms-key"
  target_key_id = aws_kms_key.re_registration_audit.id
}

data "aws_iam_policy_document" "re_registration_audit_kms_key_policy_doc" {
  statement {
    effect = "Allow"

    principals {
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
      type        = "AWS"
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }

  statement {
    effect = "Allow"

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]

    resources = ["*"]
  }

  statement {
    effect = "Allow"

    principals {
      identifiers = ["cloudwatch.amazonaws.com"]
      type        = "Service"
    }

    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]

    resources = ["*"]
  }
}