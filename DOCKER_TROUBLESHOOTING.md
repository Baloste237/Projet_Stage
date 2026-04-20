# Common Docker Issues & Solutions

## 1. Ports Already in Use

### Error Message
```
docker: Error response from daemon: driver failed programming external connectivity 
on endpoint: (xxxxxxxxx) bind: An attempt was made to use a socket address not permitted
```

### Diagnosis
```powershell
# Windows - Find process using port 5433
netstat -ano | findstr :5433

# Windows - Find process using port 8081
netstat -ano | findstr :8081

# Get process details
tasklist | findstr PID
```

### Solution
```yml
# Option 1: Use different ports in .env
DB_PORT=5434
BACKEND_PORT=8082

# Option 2: Kill existing process (Windows)
taskkill /PID <PID> /F

# Option 3: Docker port remapping in docker-compose.yml
postgres:
  ports:
    - "5434:5432"  # Changed from 5433

backend:
  ports:
    - "8082:8081"  # Changed from 8081
```

## 2. Docker Daemon Not Running

### Error Message
```
Cannot connect to the Docker daemon. Is the docker daemon running?
```

### Solution (Windows)
```bash
# Start Docker Desktop application manually, or:
docker-machine start default

# Verify
docker ps
```

### Solution (Linux)
```bash
sudo systemctl start docker
docker ps
```

## 3. Database Connection Failed

### Error Message
```
Connection refused: backend - org.postgresql.util.PSQLException: Connection refused
```

### Diagnosis
```bash
# Check PostgreSQL is running
docker-compose ps

# Check PostgreSQL logs
docker-compose logs postgres

# Test connection manually
docker exec vuln_scanner_db pg_isready -U postgres
```

### Solution
```bash
# Ensure PostgreSQL is healthy
docker-compose ps

# Wait for health check to pass
docker-compose logs postgres | grep "accepting connections"

# Rebuild from scratch
docker-compose down -v
docker-compose up -d
```

## 4. Backend Won't Start

### Error Message
```
Connection to PostgreSQL failed after retries
Spring startup failure
```

### Diagnosis
```bash
# View detailed logs
docker-compose logs -f backend

# Check environment variables
docker exec vuln_scanner_backend env | grep SPRING_DATASOURCE
```

### Solution
```bash
# Verify .env exists and is valid
cat .env

# Check database connectivity
docker exec -it vuln_scanner_db psql -U postgres -d vuln_scanner -c "SELECT 1;"

# Rebuild backend
docker-compose build --no-cache backend
docker-compose up backend
```

## 5. Permission Denied Issues

### Error Message
```
permission denied: unknown
mount denied
```

### Solution (Docker for Desktop - Windows)
```bash
# Ensure your project is in a shared drive
# Settings → Resources → File Sharing → Add C:\path\to\project
```

### Solution (Linux)
```bash
# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Check permissions
ls -la /var/run/docker.sock
```

## 6. Out of Disk Space

### Error Message
```
no space left on device
failed to create directory
```

### Solution
```bash
# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune

# Show disk usage
docker system df

# Full cleanup
docker system prune -a --volumes
```

## 7. Memory Issues

### Error Message
```
OOMKilled
Killed (exit code 137)
Container exited with code 137
```

### Solution (docker-compose.yml)
```yml
backend:
  deploy:
    resources:
      limits:
        memory: 2G

postgres:
  deploy:
    resources:
      limits:
        memory: 2G
```

### Check Memory Usage
```bash
docker stats
docker stats vuln_scanner_backend
```

## 8. Network Issues Between Containers

### Error Message
```
Name resolution failed
Cannot resolve container name
Network error
```

### Diagnosis
```bash
# Check network exists
docker network ls | grep vuln_scanner_network

# Inspect network
docker network inspect vuln_scanner_network

# Test DNS resolution
docker-compose exec backend nslookup postgres
```

### Solution
```bash
# Recreate network
docker-compose down
docker-compose up -d

# Ensure network is in docker-compose.yml
networks:
  vuln_scanner_network:
    driver: bridge

# Service must reference network
services:
  backend:
    networks:
      - vuln_scanner_network
```

## 9. Slow Build

### Error Message
```
Docker build taking very long time
```

### Solution
```bash
# Use cache (default)
docker-compose build

# Force rebuild without cache (slower)
docker-compose build --no-cache

# Check Maven cache
docker exec -it vuln_scanner_backend ls ~/.m2/repository

# Add to .dockerignore to speed up builds
target/
.git/
.gitignore
.idea/
.vscode/
*.log
```

## 10. Volume Mount Issues

### Error Message
```
mount denied
cannot mount volume
```

### Diagnosis
```bash
# Check volume exists
docker volume ls

# Inspect volume
docker volume inspect postgres_data

# Check mount path
docker inspect vuln_scanner_db | grep Mount -A 5
```

### Solution
```bash
# Remove problematic volume
docker volume rm postgres_data

# Use absolute path in docker-compose.yml
volumes:
  - /absolute/path/to/uploads:/app/uploads

# Or relative with full path
volumes:
  - ${PWD}/backend/uploads:/app/uploads
```

## 11. Image Build Failures

### Error Message
```
failed to fetch repository
no such file or directory
build error
```

### Diagnosis
```bash
# View build logs
docker-compose build --verbose

# Check Dockerfile exists
ls -la backend/Dockerfile

# Check pom.xml exists
ls -la backend/pom.xml
```

### Solution
```bash
# Verify file structure
tree backend/ -L 2

# Clear build cache and rebuild
docker-compose build --no-cache

# Try building manually
cd backend
docker build -f Dockerfile -t vuln-scanner-backend:latest .
```

## 12. Environment Variables Not Applied

### Error Message
```
Variable not recognized
Default value being used instead of .env
```

### Diagnosis
```bash
# Check .env file is in correct location
cat .env

# Verify environment in container
docker exec vuln_scanner_backend env | grep DB_DATASOURCE
```

### Solution
```bash
# Ensure .env is in same directory as docker-compose.yml
ls -la .env docker-compose.yml

# Specify .env file explicitly
docker-compose --env-file .env up -d

# Or in docker-compose.yml
services:
  backend:
    env_file: .env
```

## Command Reference

```bash
# Debug Tools
docker-compose logs -f backend           # Stream logs
docker-compose exec backend bash         # Access container shell
docker inspect <container>               # Detailed info
docker stats                            # Real-time stats
docker volume ls                        # List volumes
docker network ls                       # List networks

# Maintenance
docker-compose down -v                  # Full cleanup
docker system prune -a --volumes        # Clean everything
docker image prune -a                   # Clean unused images
docker volume prune                     # Clean unused volumes

# Database
docker exec -it vuln_scanner_db psql -U postgres -d vuln_scanner
docker exec vuln_scanner_db pg_dump -U postgres vuln_scanner > backup.sql
docker exec -i vuln_scanner_db psql -U postgres vuln_scanner < backup.sql
```

## Getting Help

1. Check Docker documentation: https://docs.docker.com/
2. View container logs: `docker-compose logs`
3. Inspect containers: `docker inspect <container>`
4. Check Docker Desktop: Troubleshoot tab
5. Run diagnostics: `docker system info`

---

**Last Updated**: March 31, 2026
