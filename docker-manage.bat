@echo off
REM Docker Compose Management Script for Windows

setlocal enabledelayedexpansion

if "%1"=="" (
    echo.
    echo Usage: docker-manage.bat [command]
    echo.
    echo Commands:
    echo   up              - Start all services
    echo   down            - Stop and remove containers
    echo   restart         - Restart all services
    echo   logs            - View logs from all services
    echo   logs-backend    - View backend logs
    echo   logs-db         - View database logs
    echo   ps              - List running containers
    echo   build           - Build all images
    echo   clean           - Remove containers and volumes
    echo   exec-db         - Access database shell
    echo   exec-backend    - Access backend container shell
    echo.
    goto :eof
)

if "%1"=="up" (
    echo Starting all services...
    docker-compose up -d
    echo All services started!
    goto :eof
)

if "%1"=="down" (
    echo Stopping services...
    docker-compose down
    echo Services stopped!
    goto :eof
)

if "%1"=="restart" (
    echo Restarting services...
    docker-compose restart
    echo Services restarted!
    goto :eof
)

if "%1"=="logs" (
    docker-compose logs -f
    goto :eof
)

if "%1"=="logs-backend" (
    docker-compose logs -f backend
    goto :eof
)

if "%1"=="logs-db" (
    docker-compose logs -f postgres
    goto :eof
)

if "%1"=="ps" (
    docker-compose ps
    goto :eof
)

if "%1"=="build" (
    echo Building images...
    docker-compose build
    echo Build complete!
    goto :eof
)

if "%1"=="clean" (
    echo Removing containers and volumes...
    docker-compose down -v
    echo Cleaned!
    goto :eof
)

if "%1"=="exec-db" (
    echo Accessing database container...
    docker exec -it vuln_scanner_db psql -U postgres -d vuln_scanner
    goto :eof
)

if "%1"=="exec-backend" (
    echo Accessing backend container...
    docker exec -it vuln_scanner_backend bash
    goto :eof
)

echo Unknown command: %1
goto :eof
