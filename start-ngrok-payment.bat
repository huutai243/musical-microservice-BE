@echo off
SETLOCAL EnableDelayedExpansion

echo.
echo ================================================
echo ===  Starting Payment Service with ngrok (API Gateway on port 9000)
echo ================================================

:: [0/5] Kill ngrok cũ nếu đang chạy
echo [0/5] Killing existing ngrok process (if any)...
taskkill /f /im ngrok.exe >nul 2>&1

:: [1/5] Kiểm tra ngrok
echo [1/5] Checking ngrok installation...
if not exist "C:\Users\HP\ngrok\ngrok.exe" (
    echo ERROR: ngrok.exe not found at C:\Users\HP\ngrok\ngrok.exe
    echo Please install ngrok or update the path in the script.
    pause
    exit /b 1
)

:: [2/5] Start ngrok trên localhost:9000 (API Gateway)
echo [2/5] Starting ngrok tunnel to http://localhost:9000 ...
start /B "" "C:\Users\HP\ngrok\ngrok.exe" http 9000 > C:\musical-microservice\ngrok.log 2>&1

:: [3/5] Chờ ngrok khởi động
echo [3/5] Waiting for ngrok to be ready...
ping 127.0.0.1 -n 11 > nul

:: [4/5] Lấy public URL từ ngrok
echo [4/5] Getting ngrok public URL...
set NGROK_URL=
set RETRY_COUNT=0
:retry
FOR /F "delims=" %%i IN ('powershell -NoProfile -Command "(Invoke-RestMethod -Uri http://127.0.0.1:4040/api/tunnels -ErrorAction SilentlyContinue).tunnels[0].public_url"') DO (
    SET NGROK_URL=%%i
)
if "!NGROK_URL!"=="" (
    set /a RETRY_COUNT+=1
    if !RETRY_COUNT! LSS 3 (
        echo Retrying to get ngrok URL... Attempt !RETRY_COUNT!
        ping 127.0.0.1 -n 6 > nul
        goto :retry
    ) else (
        echo ERROR: Could not retrieve ngrok URL after 3 attempts. Check ngrok status at http://127.0.0.1:4040 or C:\musical-microservice\ngrok.log.
        pause
        exit /b 1
    )
)

echo Found ngrok URL: !NGROK_URL!

:: [5/5] Ghi file .env
echo [5/5] Writing Stripe & PayPal URLs to .env file...

:: Xóa file cũ nếu có
if exist C:\musical-microservice\.env del C:\musical-microservice\.env

:: Ghi từng dòng, dùng dấu ngoặc tròn () để tránh lỗi hiển thị
(echo STRIPE_WEBHOOK_URL=!NGROK_URL!/api/payment/webhook) > C:\musical-microservice\.env
if errorlevel 1 (
    echo ERROR: Failed to write STRIPE_WEBHOOK_URL to .env file. Check folder permissions.
    pause
    exit /b 1
)

(echo PAYPAL_SUCCESS_URL=!NGROK_URL!/api/payment/paypal/success) >> C:\musical-microservice\.env
if errorlevel 1 (
    echo ERROR: Failed to write PAYPAL_SUCCESS_URL to .env file.
    pause
    exit /b 1
)

(echo PAYPAL_CANCEL_URL=!NGROK_URL!/api/payment/paypal/cancel) >> C:\musical-microservice\.env
if errorlevel 1 (
    echo ERROR: Failed to write PAYPAL_CANCEL_URL to .env file.
    pause
    exit /b 1
)

echo  .env file updated successfully.

:: [6/5] Khởi động Docker Compose
echo [6/5] Starting docker-compose...
cd C:\musical-microservice
docker-compose up -d

if errorlevel 1 (
    echo ERROR: Failed to start docker-compose. Check Docker installation and docker-compose.yml in C:\musical-microservice.
    pause
    exit /b 1
)

echo.
echo SYSTEM READY!
echo  Stripe Webhook:     !NGROK_URL!/api/payment/webhook
echo  PayPal Success URL: !NGROK_URL!/api/payment/paypal/success
echo  PayPal Cancel  URL: !NGROK_URL!/api/payment/paypal/cancel
echo.

ENDLOCAL
pause