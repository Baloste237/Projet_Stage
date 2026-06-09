terraform {
  required_version = ">= 1.6.0"

  required_providers {
    hcloud = {
      source  = "hetznercloud/hcloud"
      version = "~> 1.45"
    }
  }

  # ── State distant : GitLab Terraform HTTP Backend ──────────
  # Remplacer <PROJECT_ID> par l'ID numérique de votre projet GitLab
  backend "http" {
    address        = "https://gitlab.com/api/v4/projects/<PROJECT_ID>/terraform/state/production"
    lock_address   = "https://gitlab.com/api/v4/projects/<PROJECT_ID>/terraform/state/production/lock"
    unlock_address = "https://gitlab.com/api/v4/projects/<PROJECT_ID>/terraform/state/production/lock"
    username       = "gitlab-ci-token"
    password       = ""            # Fourni par CI_JOB_TOKEN dans le pipeline
    lock_method    = "POST"
    unlock_method  = "DELETE"
  }
}

provider "hcloud" {
  token = var.hcloud_token
}
