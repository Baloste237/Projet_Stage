# ── Infrastructure principale AiSecureScan ──────────────────────

# ── Clé SSH ────────────────────────────────────────────────────
resource "hcloud_ssh_key" "deployer" {
  name       = "aisecurescan-deployer"
  public_key = var.ssh_public_key
}

# ── VM Principale ───────────────────────────────────────────────
resource "hcloud_server" "aisecurescan_prod" {
  name        = var.server_name
  image       = "ubuntu-22.04"
  server_type = var.server_type
  location    = var.server_location
  ssh_keys    = [hcloud_ssh_key.deployer.id]
  firewall_ids = [hcloud_firewall.aisecurescan_fw.id]

  # Script d'initialisation minimal (Docker installé par Ansible)
  user_data = <<-EOF
    #!/bin/bash
    # Mise à jour initiale du système
    apt-get update -y && apt-get upgrade -y

    # Création de l'utilisateur deployer
    useradd -m -s /bin/bash deployer
    usermod -aG sudo deployer
    mkdir -p /home/deployer/.ssh
    cp /root/.ssh/authorized_keys /home/deployer/.ssh/authorized_keys
    chown -R deployer:deployer /home/deployer/.ssh
    chmod 700 /home/deployer/.ssh
    chmod 600 /home/deployer/.ssh/authorized_keys

    # Répertoire de déploiement
    mkdir -p /opt/aisecurescan
    chown deployer:deployer /opt/aisecurescan
  EOF

  labels = {
    environment = var.environment
    project     = "aisecurescan"
    managed_by  = "terraform"
  }
}

# ── Firewall ────────────────────────────────────────────────────
resource "hcloud_firewall" "aisecurescan_fw" {
  name = "aisecurescan-firewall"

  # SSH
  rule {
    direction  = "in"
    protocol   = "tcp"
    port       = "22"
    source_ips = ["0.0.0.0/0", "::/0"]
  }

  # HTTP (redirection vers HTTPS)
  rule {
    direction  = "in"
    protocol   = "tcp"
    port       = "80"
    source_ips = ["0.0.0.0/0", "::/0"]
  }

  # HTTPS
  rule {
    direction  = "in"
    protocol   = "tcp"
    port       = "443"
    source_ips = ["0.0.0.0/0", "::/0"]
  }

  # Grafana (accès restreint — à filtrer par IP en production)
  rule {
    direction  = "in"
    protocol   = "tcp"
    port       = "3000"
    source_ips = ["0.0.0.0/0", "::/0"]
  }

  labels = {
    project    = "aisecurescan"
    managed_by = "terraform"
  }
}

# ── IP Flottante (IP statique persistante) ──────────────────────
resource "hcloud_floating_ip" "aisecurescan_ip" {
  type          = "ipv4"
  home_location = var.server_location
  description   = "IP statique AiSecureScan production"

  labels = {
    project = "aisecurescan"
  }
}

resource "hcloud_floating_ip_assignment" "main" {
  floating_ip_id = hcloud_floating_ip.aisecurescan_ip.id
  server_id      = hcloud_server.aisecurescan_prod.id
}

# ── Volume de données persistant ────────────────────────────────
resource "hcloud_volume" "aisecurescan_data" {
  name      = "aisecurescan-data"
  size      = 50         # 50 Go
  location  = var.server_location
  format    = "ext4"

  labels = {
    project    = "aisecurescan"
    managed_by = "terraform"
  }
}

resource "hcloud_volume_attachment" "data" {
  volume_id = hcloud_volume.aisecurescan_data.id
  server_id = hcloud_server.aisecurescan_prod.id
  automount = true
}
