terraform{
      backend "s3" {
        bucket = "prm-deductions-terraform-state"
        key    = "gp-registrations-mi-forwarder/terraform.tfstate"
        region = "eu-west-2"
        encrypt = true
    }
}
