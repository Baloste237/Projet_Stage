#!/bin/bash

# Docker Compose Management Script for Linux/Mac

usage() {
  cat << EOF
Usage: ./docker-manage.sh [command]

Commands:
  up              - Start all services
  down            - Stop and remove containers
  restart         - Restart all services
  logs            - View logs from all services
  logs-backend    - View backend logs
  logs-db         - View database logs
  ps              - List running containers
  build           - Build all images
  clean           - Remove containers and volumes
  exec-db         - Access database shell
  exec-backend    - Access backend container shell
  status          - Show health status of all services
  
EOF
}

if [ -z "$1" ]; then
  usage
  exit 0
fi

case "$1" in
  up)
    echo "Starting all services..."
    docker-compose up -d
    echo "All services started!"
    docker-compose ps
    ;;
  
  down)
    echo "Stopping services..."
    docker-compose down
    echo "Services stopped!"
    ;;
  
  restart)
    echo "Restarting services..."
    docker-compose restart
    echo "Services restarted!"
    ;;
  
  logs)
    docker-compose logs -f
    ;;
  
  logs-backend)
    docker-compose logs -f backend
    ;;
  
  logs-db)
    docker-compose logs -f postgres
    ;;
  
  ps)
    docker-compose ps
    ;;
  
  build)
    echo "Building images..."
    docker-compose build
    echo "Build complete!"
    ;;
  
  clean)
    echo "Removing containers and volumes..."
    docker-compose down -v
    echo "Cleaned!"
    ;;
  
  exec-db)
    echo "Accessing database container..."
    docker exec -it vuln_scanner_db psql -U postgres -d vuln_scanner
    ;;
  
  exec-backend)
    echo "Accessing backend container..."
    docker exec -it vuln_scanner_backend bash
    ;;
  
  status)
    echo "Checking service health..."
    echo ""
    echo "Running containers:"
    docker-compose ps
    echo ""
    echo "Backend health:"
    curl -s http://localhost:8081/actuator/health | jq . 2>/dev/null || echo "Backend not responding"
    echo ""
    echo "Database health:"
    docker exec vuln_scanner_db pg_isready -U postgres || echo "Database not responding"
    ;;
  
  *)
    echo "Unknown command: $1"
    usage
    exit 1
    ;;
esac
