# 🐳 Docker Compose Structure - Complete Overview

## 📁 Files Created

```
Projet_Stage/
├── docker-compose.yml                 # Main production-ready configuration
├── docker-compose.dev.yml             # Development configuration with pgAdmin
├── docker-compose.prod.yml            # Production configuration with resource limits
├── .env                               # Environment variables (auto-generated)
├── .env.example                       # Example environment file (versioned)
├── .dockerignore_gitignore            # Git ignore rules for Docker files
├── DOCKER_README.md                   # Complete Docker documentation
├── docker-manage.bat                  # Windows management script
├── docker-manage.sh                   # Linux/Mac management script
│
├── backend/
│   ├── Dockerfile                    # Multi-stage build for Java app
│   ├── .dockerignore                 # Docker build ignore rules
│   ├── pom.xml                       # Maven configuration
│   └── ...
│
└── database/
    └── init.sql                       # PostgreSQL initialization script
```

## 🚀 Quick Reference

### Start Services
```bash
# Development (with hot reload, pgAdmin, debug port)
docker-compose -f docker-compose.dev.yml up -d

# Production (with resource limits)
docker-compose -f docker-compose.prod.yml up -d

# Standard (default)
docker-compose up -d
```

### Using Management Scripts

**Windows:**
```powershell
.\docker-manage.bat up              # Start services
.\docker-manage.bat logs-backend    # View backend logs
.\docker-manage.bat exec-db         # Access database
.\docker-manage.bat status          # Check health
```

**Linux/Mac:**
```bash
chmod +x docker-manage.sh           # Make executable
./docker-manage.sh up               # Start services
./docker-manage.sh logs-backend     # View backend logs
./docker-manage.sh exec-db          # Access database
./docker-manage.sh status           # Check health
```

## 📊 Services Configuration

### PostgreSQL (Base Config)
| Property | Value |
|----------|-------|
| Image | postgres:16-alpine |
| Container | vuln_scanner_db |
| Port | 5433 → 5432 |
| Database | vuln_scanner |
| Health Check | Every 10s |

### Backend (Base Config)
| Property | Value |
|----------|-------|
| Framework | Spring Boot 4.0.1 |
| Java | JDK 21 |
| Container | vuln_scanner_backend |
| Port | 8081 |
| Health Check | Every 15s (Actuator) |

### pgAdmin (Dev Only)
| Property | Value |
|----------|-------|
| Container | vuln_scanner_pgadmin |
| Port | 5050 → 80 |
| URL | http://localhost:5050 |
| Email | admin@example.com |
| Password | admin |

## 🔧 Configuration Profiles

### docker-compose.yml
✅ Standard production-ready configuration
- PostgreSQL with Alpine Linux
- Multi-stage build for backend
- Health checks enabled
- Volume persistence
- Network isolation

### docker-compose.dev.yml
✅ Development tools and features
- pgAdmin included for database management
- Debug port (5005) exposed
- Live development capabilities
- Better logging
- Volume mounts for code access

### docker-compose.prod.yml
✅ Production-grade security and performance
- Resource limits (CPU: 2-4, Memory: 1-2GB)
- Restart policies (unless-stopped)
- JVM optimization flags
- Strong password requirements
- Extended health check timeouts

## 📝 Important Notes

1. **Environment Variables (.env)**
   - Generated automatically on first use
   - Copy from .env.example if needed
   - Contains sensitive data - NEVER commit to git

2. **Database Persistence**
   - Data stored in Docker volumes (postgres_data)
   - Survives container restarts
   - Clean with `docker-compose down -v`

3. **Initial Setup**
   - database/init.sql runs on first startup
   - Creates extensions and indexes
   - Adjust for your schema as needed

4. **Port Conflicts**
   - PostgreSQL: 5433
   - Backend: 8081
   - pgAdmin (dev): 5050
   - Debug (dev): 5005
   - Modify in .env if needed

## 🔐 Security Considerations

### Development
✅ Acceptable defaults for local development
- Simple passwords
- No SSL/TLS required
- Debug port exposed (intentional)

### Production
⚠️ MUST implement before deploying:
1. Strong PostgreSQL password (32+ chars)
2. Use environment secrets management
3. Enable SSL/TLS for database
4. Restrict network access
5. Use health checks and monitoring
6. Enable container restart policies
7. Set resource limits
8. Use specific image versions

## 🛠️ Common Operations

```bash
# View all containers
docker-compose ps

# View logs
docker-compose logs -f

# Rebuild images
docker-compose build --no-cache

# Stop containers (keep volumes)
docker-compose stop

# Restart services
docker-compose restart

# Full cleanup (removes everything)
docker-compose down -v

# Execute in container
docker exec -it vuln_scanner_backend bash
docker exec -it vuln_scanner_db psql -U postgres

# Database backup
docker exec vuln_scanner_db pg_dump -U postgres vuln_scanner > backup.sql

# Database restore
docker exec -i vuln_scanner_db psql -U postgres vuln_scanner < backup.sql
```

## 📈 Scaling and Monitoring

### To scale backend services (if needed)
```bash
docker-compose up -d --scale backend=3
```

### Monitor resource usage
```bash
docker stats
```

### View detailed logs
```bash
docker-compose logs --tail=100 backend
```

## 🐛 Troubleshooting

### Backend won't start
```bash
docker-compose logs backend
```

### Database connection refused
```bash
docker-compose ps
docker-compose logs postgres
```

### Port already in use
```bash
# List processes using ports
netstat -ano | findstr :5433
netstat -ano | findstr :8081

# Change in .env or docker-compose.yml
```

### Permission denied errors
```bash
# On Linux, run with sudo or add user to docker group
sudo usermod -aG docker $USER
```

## 📚 Documentation Links

- [docker-compose.yml](docker-compose.yml) - Main configuration
- [Dockerfile](backend/Dockerfile) - Backend build
- [DOCKER_README.md](DOCKER_README.md) - Detailed documentation
- [.env.example](.env.example) - Configuration template

## ✅ Verification Checklist

After starting services:
- [ ] `docker-compose ps` shows all containers running
- [ ] `curl http://localhost:8081/actuator/health` returns UP
- [ ] Database accessible via `psql -h localhost -p 5433 -U postgres`
- [ ] pgAdmin (dev) accessible at http://localhost:5050
- [ ] No error messages in `docker-compose logs`
- [ ] Uploads directory writable at `./backend/uploads`

---

**Last Updated**: March 31, 2026
**Version**: 1.0
