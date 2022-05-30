variable "region" {
  type    = string
  default = "eu-west-2"
}

variable "repo_name" {
  type = string
  default = "prm-repo-re-registration-service"
}

variable "service_desired_count" {}

variable "environment" {}

variable "component_name" {
  default = "re-registration-service"
}

variable "task_image_tag" {}

variable "task_cpu" {
  default = 512
}
variable "task_memory" {
  default = 1024
}

variable "log_level" {
  type = string
  default = "debug"
}
