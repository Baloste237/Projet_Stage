# 🚀 Guide de Déploiement — AiSecureScan

Ce dépôt contient maintenant toute l'infrastructure as code (IaC) pour déployer le projet en production avec **Terraform**, **Ansible**, **Nginx HTTPS**, et une stack de **Monitoring complète**.

---

## 🏗️ 1. Terraform (Création de la VM)

Les fichiers sont dans le dossier `terraform/`. Ils utilisent **Hetzner Cloud** (très peu cher et performant), mais peuvent être adaptés.

1. Allez dans le dossier `terraform/`
2. Copiez le fichier de variables : `cp terraform.tfvars.example terraform.tfvars`
3. Ajoutez votre token API Hetzner et votre clé SSH publique.
4. Lancez les commandes :
   ```bash
   terraform init
   terraform plan
   terraform apply
   ```
5. Terraform vous donnera l'IP de la VM créée.

---

## ⚙️ 2. Ansible (Configuration de la VM)

Les fichiers sont dans le dossier `ansible/`.

1. Éditez le fichier `ansible/inventory/production.ini` en remplaçant `<IP_SERVEUR>` par l'IP donnée par Terraform.
2. Créez votre fichier `.env.prod` à la racine (basé sur `.env.prod.template`).
3. Lancez le déploiement :
   ```bash
   cd ansible
   ansible-playbook -i inventory/production.ini playbooks/deploy-app.yml -K
   ```
> Ansible va installer Docker, copier tous les fichiers, se connecter à GitLab, pull les images et tout démarrer.

---

## 🔒 3. Nginx & HTTPS

Le fichier `nginx/nginx.conf` est configuré pour une production sécurisée :
- Redirection automatique HTTP vers HTTPS.
- Utilisation des certificats SSL **Let's Encrypt** (Certbot).
- Headers de sécurité (HSTS, X-Frame-Options, etc.).
- Gère le frontend (`/`), l'API backend (`/api`), Swagger (`/swagger-ui`) et MobSF (`/mobsf`).

> Vous devrez lancer la commande `certbot` sur votre serveur Ubuntu manuellement une fois pour générer les certificats Let's Encrypt (voir le document d'implémentation, Phase 8).

---

## 📊 4. Stack Monitoring

Une stack complète a été générée via `docker-compose.monitoring.yml`.

- **Prometheus** (Port 9090) : Scrape les métriques du backend Java, de l'API Python, etc.
- **Grafana** (Port 3000) : Interface visuelle de monitoring.
- **Loki** (Port 3100) : Base de données des logs.
- **Promtail** : Capture les logs de vos conteneurs Docker (AiSecureScan) et les envoie à Loki.
- **Node Exporter & cAdvisor** : Remontent les infos de votre CPU, RAM, disques et conteneurs Docker.

**Pour y accéder :**
Ouvrez `http://<IP_SERVEUR>:3000` (Grafana). Le mot de passe par défaut est `admin` (modifiable via le `.env.prod`).
Les sources de données Loki et Prometheus sont **déjà pré-configurées** automatiquement.
