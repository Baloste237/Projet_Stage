# 🚀 Docker Compose Quick Start

## Prerequisites
- Docker installed (https://www.docker.com/products/docker-desktop)
- Docker Compose installed (included with Docker Desktop)
- Git for version control

### Verify Installation
```bash
docker --version
docker-compose --version
```

---

## 🎯 Quick Start (2 minutes)

### 1. Navigate to Project
```bash
cd e:\Projet_stage\Projet_Stage
```

### 2. Start Services
```bash
docker-compose up -d
```

### 3. Verify Running
```bash
docker-compose ps
```

You should see:
- ✅ vuln_scanner_db (running)
- ✅ vuln_scanner_backend (running)

---

## 📱 Access Services

### Backend API
- **URL**: http://localhost:8081
- **Health Check**: http://localhost:8081/actuator/health
- **Register**: POST http://localhost:8081/api/auth/register
- **Login**: POST http://localhost:8081/api/auth/login

### Database
```bash
# Connect with psql
psql -h localhost -p 5433 -U postgres -d vuln_scanner

# Or via Docker
docker-compose exec postgres psql -U postgres -d vuln_scanner
```

---

## 🔧 Management Commands

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f postgres
```

### Using Helper Script (Windows)
```batch
docker-manage.bat up              # Start
docker-manage.bat logs-backend    # View backend logs
docker-manage.bat ps              # List containers
docker-manage.bat status          # Check health
docker-manage.bat down            # Stop
```

### Using Helper Script (Linux/Mac)
```bash
chmod +x docker-manage.sh
./docker-manage.sh up
./docker-manage.sh logs-backend
./docker-manage.sh status
```

---

## 🛑 Stop Services

```bash
# Stop containers (keep data)
docker-compose stop

# Stop and remove containers (keep data)
docker-compose down

# Stop, remove containers AND delete all data
docker-compose down -v
```

---

## 🔄 Development Workflow

### With Auto-Reload (pgAdmin + Debug)
```bash
docker-compose -f docker-compose.dev.yml up -d
```

**Additional Access:**
- pgAdmin: http://localhost:5050
- Backend Debug: localhost:5005 (IDE breakpoints)
- Email: admin@example.com / Password: admin

### Production Setup
```bash
# Update .env with production values
docker-compose -f docker-compose.prod.yml up -d
```

---

## ⚙️ Configuration

### Environment Variables (.env)
```env
DB_NAME=vuln_scanner
DB_USER=postgres
DB_PASSWORD=admin
DB_PORT=5433
BACKEND_PORT=8081
ENVIRONMENT=development
```

**⚠️ For production**, change:
- `DB_PASSWORD` to a strong password
- `ENVIRONMENT=production`
- Use `.env.prod.template` as guide

---

## 🐛 Common Issues

### Port in Use
```bash
# Change in .env
DB_PORT=5434
BACKEND_PORT=8082
```

### Backend won't connect to DB
```bash
# Check PostgreSQL status
docker-compose logs postgres

# Restart backend
docker-compose restart backend
```

### Need to rebuild
```bash
docker-compose build --no-cache
docker-compose up -d
```

See **[DOCKER_TROUBLESHOOTING.md](DOCKER_TROUBLESHOOTING.md)** for more issues.

---

## 📚 Full Documentation

- **[DOCKER_README.md](DOCKER_README.md)** - Complete guide
- **[DOCKER_STRUCTURE.md](DOCKER_STRUCTURE.md)** - Structure overview
- **[DOCKER_TROUBLESHOOTING.md](DOCKER_TROUBLESHOOTING.md)** - Problem solving

---

## 🎓 Next Steps

1. ✅ Verify services running: `docker-compose ps`
2. ✅ Test backend: `curl http://localhost:8081/actuator/health`
3. ✅ Access database: `docker-compose exec postgres psql -U postgres -d vuln_scanner`
4. ✅ View logs: `docker-compose logs -f`
5. 📖 Read documentation for advanced setup

---

**You're all set! 🎉**

For detailed information, see the documentation files listed above.
