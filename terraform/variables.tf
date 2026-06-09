# ── Variables Terraform ────────────────────────────────────────

variable "hcloud_token" {
  description = "Token API Hetzner Cloud"
  type        = string
  sensitive   = true
}

variable "server_name" {
  description = "Nom de la VM de production"
  type        = string
  default     = "aisecurescan-production"
}

variable "server_type" {
  description = "Type de serveur Hetzner (cx21 = 2vCPU / 4GB RAM)"
  type        = string
  default     = "cx21"
}

variable "server_location" {
  description = "Localisation du datacenter Hetzner"
  type        = string
  default     = "fsn1"   # Falkenstein, Allemagne
}

variable "ssh_public_key" {
  description = "Clé SSH publique pour accès au serveur"
  type        = string
}

variable "environment" {
  description = "Environnement cible (production / staging)"
  type        = string
  default     = "production"
}
