# ✅ Docker Setup Verification Checklist

Use this checklist to verify your Docker setup is complete and working correctly.

## 📁 File Structure

- [ ] `docker-compose.yml` exists in project root
- [ ] `docker-compose.dev.yml` exists in project root
- [ ] `docker-compose.prod.yml` exists in project root
- [ ] `.env` file exists (or created from template)
- [ ] `.env.example` exists with sample values
- [ ] `backend/Dockerfile` exists
- [ ] `backend/.dockerignore` exists
- [ ] `database/init.sql` exists
- [ ] `docker-manage.bat` exists (Windows)
- [ ] `docker-manage.sh` exists (Linux/Mac)

## 🐳 Docker Installation

- [ ] Docker Desktop installed
- [ ] Docker daemon running (`docker --version` works)
- [ ] Docker Compose installed (`docker-compose --version` works)
- [ ] Port 5433 available for PostgreSQL
- [ ] Port 8081 available for Backend

### Verify
```bash
docker --version
docker-compose --version
docker ps
```

## 🔧 Configuration

### .env File
- [ ] `.env` file exists in project root
- [ ] `DB_NAME=vuln_scanner`
- [ ] `DB_USER=postgres`
- [ ] `DB_PASSWORD` is set (not empty)
- [ ] `DB_PORT=5433`
- [ ] `BACKEND_PORT=8081`
- [ ] `ENVIRONMENT=development` (dev mode)

```bash
# View configuration
cat .env
```

### Dockerfile
- [ ] `backend/Dockerfile` exists
- [ ] Multi-stage build is present
- [ ] JDK 21 is used (eclipse-temurin:21-jre-alpine)
- [ ] Maven build stage present
- [ ] Port 8081 exposed
- [ ] Health checks work

```bash
# Check Dockerfile syntax
docker build --dry-run -f backend/Dockerfile backend/
```

## 🚀 Services Startup

### Build Images (First Time)
- [ ] Run: `docker-compose build`
- [ ] No build errors
- [ ] Backend image created
- [ ] PostgreSQL image pulled

```bash
# Verify images built
docker images | grep vuln-scanner
docker images | grep postgres
```

### Start Services
- [ ] Run: `docker-compose up -d`
- [ ] No error messages
- [ ] All services starting

```bash
# Check status
docker-compose ps
```

## 🏥 Health Checks

### PostgreSQL Status
- [ ] Container `vuln_scanner_db` is **running**
- [ ] Health status **healthy**
- [ ] Port 5433 accepting connections

```bash
# Verify
docker-compose ps postgres
docker logs vuln_scanner_db | grep "accepting connections"
```

### Backend Status
- [ ] Container `vuln_scanner_backend` is **running**
- [ ] Health status **healthy**
- [ ] Spring Boot started successfully

```bash
# Verify
docker-compose ps backend
curl http://localhost:8081/actuator/health
```

## 📊 Data & Volumes

### Docker Volumes
- [ ] Volume `postgres_data` created
- [ ] Database data persisted
- [ ] Uploads directory mounted

```bash
# List volumes
docker volume ls | grep postgres_data
docker volume inspect postgres_data
```

### Database Initialization
- [ ] Database `vuln_scanner` exists
- [ ] Tables auto-created by Hibernate
- [ ] Extensions created (`uuid-ossp`, `pgcrypto`)

```bash
# Check database
docker-compose exec postgres psql -U postgres -d vuln_scanner -c "\dt"
```

## 🌐 Connectivity Tests

### Backend API
- [ ] Health endpoint responds: `curl http://localhost:8081/actuator/health`
- [ ] Returns `{"status":"UP"}`
- [ ] No connection errors

### Database Connection
- [ ] Backend can connect to PostgreSQL
- [ ] No connection refused errors in logs
- [ ] Check logs: `docker-compose logs backend`

### Container Network
- [ ] Containers on same network
- [ ] Name resolution working
- [ ] Inter-container communication working

```bash
# Test connection from backend to database
docker-compose exec backend ping postgres
docker-compose exec backend nc -zv postgres 5432
```

## 📚 Documentation

- [ ] `DOCKER_README.md` - Complete documentation
- [ ] `DOCKER_STRUCTURE.md` - File structure overview
- [ ] `DOCKER_TROUBLESHOOTING.md` - Troubleshooting guide
- [ ] `QUICK_START.md` - Quick start instructions
- [ ] `.env.example` - Configuration template
- [ ] `.env.prod.template` - Production template

## 🔄 Services Management

### Start/Stop Scripts
- [ ] Windows script `docker-manage.bat` works
- [ ] Linux/Mac script `docker-manage.sh` executable
- [ ] Help shows with no arguments

```bash
# Windows
.\docker-manage.bat

# Linux/Mac
./docker-manage.sh
```

## 🧪 Functional Tests

### API Endpoints
```bash
# Test register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"userName":"testuser","password":"testpass123","email":"test@example.com"}'

# Test login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"testuser","password":"testpass123"}'

# Check health
curl http://localhost:8081/actuator/health
```

### Database Operations
```bash
# List tables
docker-compose exec postgres psql -U postgres -d vuln_scanner -c "\dt"

# Check user count
docker-compose exec postgres psql -U postgres -d vuln_scanner -c "SELECT COUNT(*) FROM user_info;"
```

## 🐛 Debugging Capability

### Log Viewing
- [ ] Can view all logs: `docker-compose logs`
- [ ] Can view specific service logs: `docker-compose logs backend`
- [ ] Can follow logs in real-time: `docker-compose logs -f`

### Container Access
- [ ] Can access backend: `docker-compose exec backend bash`
- [ ] Can access database: `docker-compose exec postgres bash`
- [ ] Can run psql: `docker-compose exec postgres psql -U postgres`

## 🛡️ Development vs Production

### Development Setup
- [ ] Using `docker-compose.yml` or `docker-compose.dev.yml`
- [ ] pgAdmin available (if using dev) on http://localhost:5050
- [ ] Debug port 5005 exposed (if using dev)
- [ ] Logging is verbose

### Production Readiness
- [ ] `.env.prod.template` reviewed
- [ ] Strong passwords configured
- [ ] Resource limits in `docker-compose.prod.yml`
- [ ] Restart policies enabled
- [ ] Health checks configured

## 🧹 Git Configuration

### Ignore Rules
- [ ] `.gitignore` includes `.env` (secrets ignored)
- [ ] `.gitignore` includes `postgres_data/` (data ignored)
- [ ] `.gitignore` includes `target/` (build artifacts ignored)
- [ ] `.gitignore` includes `.dockerignore_gitignore`
- [ ] `.env.example` is versioned (template only)

```bash
# Verify
git status
```

## ✨ Final Verification

### All Containers Running
```bash
docker-compose ps
```

Expected output:
```
NAME                    STATUS              PORTS
vuln_scanner_db         Up (healthy)        0.0.0.0:5433->5432/tcp
vuln_scanner_backend    Up (healthy)        0.0.0.0:8081->8081/tcp
```

### Quick Access
- [ ] Backend: http://localhost:8081 ✅
- [ ] Health: http://localhost:8081/actuator/health ✅
- [ ] Database: psql localhost 5433 ✅
- [ ] Logs: docker-compose logs ✅

---

## 📝 Notes

### Issues Found
Document any issues encountered:
- Issue: ___________________
- Solution: ___________________

### Customizations Made
Document any customizations:
- Change: ___________________
- Reason: ___________________

---

## 🎉 Completion

When all checkboxes are checked:
1. ✅ Docker setup is complete
2. ✅ All services running
3. ✅ Health checks passing
4. ✅ Database connected
5. ✅ API responding
6. ✅ Ready for development

**Next Steps:**
- Start developing! 🚀
- Refer to documentation as needed
- Track issues using DOCKER_TROUBLESHOOTING.md

---

**Checklist Version**: 1.0
**Last Updated**: March 31, 2026
