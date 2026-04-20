# Docker Compose Configuration - Vulnerability Scanner

## 📋 Overview

This Docker Compose setup provides a complete containerized environment for the Vulnerability Scanner application with the following services:

- **PostgreSQL 16** - Database (Port 5433)
- **Backend Application** - Spring Boot Java (Port 8081)

## 📁 Project Structure

```
Projet_Stage/
├── docker-compose.yml          # Main Docker Compose configuration
├── .env                        # Environment variables
├── .dockerignore               # Files to ignore when building Docker images
├── database/
│   └── init.sql               # Database initialization script
└── backend/
    ├── Dockerfile             # Backend build configuration
    ├── pom.xml                # Maven dependencies
    └── src/                   # Source code
```

## 🚀 Quick Start

### Prerequisites
- Docker (version 20.10+)
- Docker Compose (version 1.29+)

### Starting Services

1. **Navigate to project root:**
```bash
cd e:\Projet_stage\Projet_Stage
```

2. **Start all services:**
```bash
docker-compose up -d
```

3. **View logs:**
```bash
docker-compose logs -f backend
docker-compose logs -f postgres
```

4. **Stop services:**
```bash
docker-compose down
```

## 📦 Services Configuration

### PostgreSQL Database
- **Container Name**: `vuln_scanner_db`
- **Image**: `postgres:16-alpine`
- **Port**: 5433 (external) → 5432 (internal)
- **Database**: `vuln_scanner`
- **User**: `postgres`
- **Password**: `admin` (configure in `.env`)
- **Health Check**: Enabled every 10 seconds

### Backend Application
- **Container Name**: `vuln_scanner_backend`
- **Port**: 8081
- **Build**: Multi-stage build with Maven 3.9 and Java 21
- **Health Check**: Enabled via Spring Boot Actuator
- **Depends On**: PostgreSQL (waits for healthy status)
- **Volumes**: Mounts `./backend/uploads` for file storage

## 🔧 Configuration

### Environment Variables (`.env`)

```env
# Database Configuration
DB_NAME=vuln_scanner
DB_USER=postgres
DB_PASSWORD=admin
DB_PORT=5433

# Backend Configuration
BACKEND_PORT=8081

# Environment
ENVIRONMENT=development
```

Modify these values in `.env` file before starting services. The Docker Compose will automatically use these values.

## 📊 Database

### Initial Schema
- Tables are auto-created by Spring Data JPA (Hibernate)
- Useful indexes are pre-created in `database/init.sql`
- Extensions: `uuid-ossp`, `pgcrypto`

### Accessing Database

**From host:**
```bash
psql -h localhost -p 5433 -U postgres -d vuln_scanner
```

**From within containers:**
```bash
docker exec -it vuln_scanner_db psql -U postgres -d vuln_scanner
```

## 🌐 Health Checks

### Backend Health
- **Endpoint**: `http://localhost:8081/actuator/health`
- **Status**: UP when PostgreSQL is ready
- **Check Interval**: 15 seconds

### Database Health
- **Check Method**: `pg_isready`
- **Check Interval**: 10 seconds

## 📝 Useful Commands

### View running containers
```bash
docker-compose ps
```

### View container logs
```bash
docker-compose logs -f
```

### Stop containers
```bash
docker-compose stop
```

### Remove containers and volumes
```bash
docker-compose down -v
```

### Rebuild images
```bash
docker-compose build --no-cache
```

### Execute command in container
```bash
docker exec -it vuln_scanner_backend bash
docker exec -it vuln_scanner_db bash
```

## 🔐 Security Notes

- ⚠️ **Change default PostgreSQL password** in `.env` for production
- ⚠️ **Use environment-specific configurations** for prod/staging
- ⚠️ **Never commit sensitive credentials** to version control
- Use strong passwords: `openssl rand -base64 32`

## 📱 API Endpoints

Once running, the backend API is available at:
- **Base URL**: `http://localhost:8081`
- **Health Check**: `http://localhost:8081/actuator/health`
- **Authentication Register**: `POST http://localhost:8081/api/auth/register`
- **Authentication Login**: `POST http://localhost:8081/api/auth/login`

## 🐛 Troubleshooting

### Container fails to start
```bash
docker-compose logs backend
```

### Database connection errors
- Ensure PostgreSQL is healthy: `docker-compose ps`
- Check environment variables in `.env`
- Verify port 5433 is not in use: `netstat -ano | findstr :5433`

### Port already in use
Change ports in `.env` or `docker-compose.yml`:
```yaml
ports:
  - "5434:5432"  # Change 5433 to 5434
  - "8082:8081"  # Change 8081 to 8082
```

### Slow build
- First build is slower due to Maven dependencies
- Subsequent builds use cached layers
- Use `docker-compose build --no-cache` to rebuild from scratch

## 📤 Volumes

- **postgres_data**: Persistent PostgreSQL data
- **./backend/uploads**: Application file uploads

To clear all data:
```bash
docker-compose down -v
```

## 🔄 Redeploy Changes

After modifying code:

```bash
# Rebuild images
docker-compose build

# Restart services
docker-compose up -d
```

## 📚 References

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Docker Guide](https://hub.docker.com/_/postgres)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
