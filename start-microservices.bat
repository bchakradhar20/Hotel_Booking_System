@echo off
echo Starting Hotel Reservation Microservices...
echo.

REM Change to microservices directory
cd /d "%~dp0\microservices"

REM Check if Maven is available
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Maven not found! Please install Maven and add it to PATH.
    pause
    exit /b 1
)

echo Building all microservices...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting services in order...
echo.

REM Start Eureka Server first
echo [1/6] Starting Eureka Server on port 8761...
start "Eureka Server" cmd /c "cd eureka-server && mvn spring-boot:run"
echo Waiting 30 seconds for Eureka Server to start...
timeout /t 30 >nul

REM Start API Gateway
echo [2/6] Starting API Gateway on port 8085...
start "API Gateway" cmd /c "cd api-gateway && mvn spring-boot:run"
echo Waiting 20 seconds for API Gateway to start...
timeout /t 20 >nul

REM Start Auth Service
echo [3/6] Starting Auth Service on port 8081...
start "Auth Service" cmd /c "cd auth-service && mvn spring-boot:run"
echo Waiting 20 seconds for Auth Service to start...
timeout /t 20 >nul

REM Start Room Service
echo [4/6] Starting Room Service on port 8082...
start "Room Service" cmd /c "cd room-service && mvn spring-boot:run"
echo Waiting 20 seconds for Room Service to start...
timeout /t 20 >nul

REM Start Reservation Service
echo [5/6] Starting Reservation Service on port 8083...
start "Reservation Service" cmd /c "cd reservation-service && mvn spring-boot:run"
echo Waiting 20 seconds for Reservation Service to start...
timeout /t 20 >nul

REM Start User Service
echo [6/6] Starting User Service on port 8084...
start "User Service" cmd /c "cd user-service && mvn spring-boot:run"

echo.
echo ==========================================
echo All microservices are starting up!
echo ==========================================
echo.
echo Service URLs:
echo - Eureka Server:     http://localhost:8761
echo - API Gateway:       http://localhost:8085
echo - Auth Service:      http://localhost:8081
echo - Room Service:      http://localhost:8082
echo - Reservation Service: http://localhost:8083
echo - User Service:      http://localhost:8084
echo.
echo Swagger Documentation:
echo - API Gateway (All Services): http://localhost:8085/swagger-ui.html
echo - Auth Service:      http://localhost:8081/swagger-ui/index.html
echo - Room Service:      http://localhost:8082/swagger-ui/index.html
echo - Reservation Service: http://localhost:8083/swagger-ui/index.html
echo - User Service:      http://localhost:8084/swagger-ui/index.html
echo.
echo Press any key to exit...
pause >nul