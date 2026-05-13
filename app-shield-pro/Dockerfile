# Stage 1: Build
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
# Add custom nginx config if needed (optional here, we'll have a main reverse proxy)
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
