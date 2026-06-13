@echo off
echo Checking Hotel Reservation Microservices Status...
echo.

echo Service Status (checking ports):
echo ================================

REM Check each port
echo Eureka Server (8761):
netstat -an | find ":8761" | find "LISTENING" >nul && echo   [RUNNING] || echo   [STOPPED]

echo API Gateway (8085):
netstat -an | find ":8085" | find "LISTENING" >nul && echo   [RUNNING] || echo   [STOPPED]

echo Auth Service (8081):
netstat -an | find ":8081" | find "LISTENING" >nul && echo   [RUNNING] || echo   [STOPPED]

echo Room Service (8082):
netstat -an | find ":8082" | find "LISTENING" >nul && echo   [RUNNING] || echo   [STOPPED]

echo Reservation Service (8083):
netstat -an | find ":8083" | find "LISTENING" >nul && echo   [RUNNING] || echo   [STOPPED]

echo User Service (8084):
netstat -an | find ":8084" | find "LISTENING" >nul && echo   [RUNNING] || echo   [STOPPED]

echo.
echo Health Check URLs:
echo ==================
echo - Eureka Dashboard: http://localhost:8761
echo - API Gateway Health: http://localhost:8085/actuator/health
echo - Auth Service Health: http://localhost:8081/actuator/health
echo - Room Service Health: http://localhost:8082/actuator/health
echo - Reservation Service Health: http://localhost:8083/actuator/health
echo - User Service Health: http://localhost:8084/actuator/health
echo.
pause