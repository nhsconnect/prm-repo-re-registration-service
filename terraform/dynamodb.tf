resource "aws_dynamodb_table" "active_suspensions_details" {
  name = "${var.environment}-${var.component_name}-active-suspensions-details-dynamodb"
  billing_mode = "PAY_PER_REQUEST"
  hash_key = "nhs_number"

  server_side_encryption {
    enabled =  true
    kms_key_arn = aws_kms_key.active_suspensions_details_dynamodb_kms_key.arn
  }

  point_in_time_recovery {
    enabled =  true
  }

  attribute {
    name = "nhs_number"
    type = "S"
  }
}