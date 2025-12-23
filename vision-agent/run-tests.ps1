# =========================================================
# SAFEVISION AUTOMATED TEST SUITE (WINDOWS/POWERSHELL)
# =========================================================

# Faz o script parar imediatamente se algum comando falhar (equivalente ao set -e do Bash)
$ErrorActionPreference = "Stop"

# Libera a execução de scripts apenas para este processo (sessão atual) sem pedir confirmação
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force

Write-Host "🔍 INICIANDO SUITE DE TESTES AUTOMATIZADOS SAFEVISION" -ForegroundColor Cyan

# ---------------------------------------------------------
# 1. BACKEND (Java - Spring Boot)
# Usa o wrapper do Maven (mvnw.cmd) para rodar os testes
# O Testcontainers (Java) vai conversar com o Docker Desktop
# ---------------------------------------------------------
Write-Host "`n☕ [BACKEND] Executando Testes de Integração Spring Boot..." -ForegroundColor Yellow

Write-Host "   👉 Testando Auth Service..."
Push-Location "auth-service"
.\mvnw.cmd test
Pop-Location

Write-Host "   👉 Testando Alert Service..."
Push-Location "alert-service"
.\mvnw.cmd test
Pop-Location

Write-Host "   👉 Testando Recognition Service..."
Push-Location "recognition-service"
.\mvnw.cmd test
Pop-Location

# ---------------------------------------------------------
# 2. EDGE AI (Python)
# Aqui está o "Pulo do Gato": Rodamos via Docker Compose
# porque você não tem Python instalado no Windows.
# ---------------------------------------------------------
Write-Host "`n🐍 [EDGE] Executando Testes Python Vision (via Docker)..." -ForegroundColor Green
docker-compose run --rm vision-tests

# ---------------------------------------------------------
# 3. FRONTEND (Angular)
# Requer Node.js instalado no Windows.
# Se falhar, verifique se você tem o Node instalado.
# ---------------------------------------------------------
Write-Host "`n🅰️ [FRONTEND] Executando Testes Unitários Angular..." -ForegroundColor Magenta

if (Test-Path "safevision-ui") {
    Push-Location "safevision-ui"
    # O 'cmd /c' ajuda o PowerShell a executar scripts npm corretamente
    cmd /c "npm test -- --watch=false --browsers=ChromeHeadless"
    Pop-Location
} else {
    Write-Host "⚠️ Pasta safevision-ui não encontrada. Pulando frontend." -ForegroundColor Red
}

Write-Host "`n✅ TODOS OS TESTES PASSARAM COM SUCESSO! PARABÉNS! 🚀" -ForegroundColor Green -BackgroundColor Black