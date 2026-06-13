@echo off
echo Stopping Hotel Reservation Microservices...
echo.

REM Kill all Java processes running Spring Boot applications
echo Stopping all Spring Boot microservices...

REM Kill processes by window title
taskkill /f /fi "windowtitle eq Eureka Server*" 2>nul
taskkill /f /fi "windowtitle eq API Gateway*" 2>nul
taskkill /f /fi "windowtitle eq Auth Service*" 2>nul
taskkill /f /fi "windowtitle eq Room Service*" 2>nul
taskkill /f /fi "windowtitle eq Reservation Service*" 2>nul
taskkill /f /fi "windowtitle eq User Service*" 2>nul

REM Alternative: Kill Java processes on specific ports
echo Stopping services on specific ports...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8761" ^| find "LISTENING"') do taskkill /f /pid %%a 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8085" ^| find "LISTENING"') do taskkill /f /pid %%a 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8081" ^| find "LISTENING"') do taskkill /f /pid %%a 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8082" ^| find "LISTENING"') do taskkill /f /pid %%a 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8083" ^| find "LISTENING"') do taskkill /f /pid %%a 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8084" ^| find "LISTENING"') do taskkill /f /pid %%a 2>nul

echo.
echo All microservices stopped.
echo.
pause