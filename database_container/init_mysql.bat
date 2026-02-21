:: Check all Windows CMD commands https://ss64.com/nt/
@echo off
setlocal

:: Step 1: Stop and remove any existing container and volume
echo Stopping and removing any existing MySQL container and volume...
docker-compose down -v

echo DB stopped!
timeout /t 3 >nul

:: Step 2: Start up MySQL container
echo Starting MySQL container...
docker-compose up -d

:: Step 3: Wait for MySQL to be ready
echo Waiting for MySQL to initialize...
timeout /t 15 >nul

echo Done.
endlocal
pause