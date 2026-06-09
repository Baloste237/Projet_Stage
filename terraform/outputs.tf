# ── Outputs Terraform ───────────────────────────────────────────

output "server_ipv4" {
  description = "Adresse IPv4 principale du serveur"
  value       = hcloud_server.aisecurescan_prod.ipv4_address
}

output "floating_ip" {
  description = "IP statique flottante (utiliser cette IP pour le DNS)"
  value       = hcloud_floating_ip.aisecurescan_ip.ip_address
}

output "server_id" {
  description = "ID du serveur Hetzner"
  value       = hcloud_server.aisecurescan_prod.id
}

output "volume_id" {
  description = "ID du volume de données"
  value       = hcloud_volume.aisecurescan_data.id
}

output "ssh_command" {
  description = "Commande SSH pour se connecter au serveur"
  value       = "ssh -i ~/.ssh/aisecurescan_deploy deployer@${hcloud_floating_ip.aisecurescan_ip.ip_address}"
}

output "ansible_command" {
  description = "Commande Ansible pour déployer"
  value       = "ansible-playbook -i ansible/inventory/production.ini ansible/playbooks/deploy-app.yml"
}
