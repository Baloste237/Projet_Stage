FROM nginx:1.25-alpine

# Copier la configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Créer le répertoire pour Certbot ACME challenge
RUN mkdir -p /var/www/certbot

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
