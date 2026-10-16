# ===========================================================================
# Mesa 2.0 — build local de punta a punta: SPA + tests + jar único con la SPA
# dentro (el mismo resultado que produce el Dockerfile).
#
#   Uso:  ./build-all.ps1
# ===========================================================================
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host '== Frontend: dependencias ==' -ForegroundColor Cyan
Push-Location "$raiz/frontend"
npm ci

Write-Host '== Frontend: lint + tests ==' -ForegroundColor Cyan
npm run lint
npm test

Write-Host '== Frontend: compilación ==' -ForegroundColor Cyan
npm run build
Pop-Location

Write-Host '== Copiando la SPA al backend ==' -ForegroundColor Cyan
$destino = "$raiz/backend/src/main/resources/static"
New-Item -ItemType Directory -Force -Path $destino | Out-Null
Copy-Item -Recurse -Force "$raiz/frontend/dist/*" $destino

Write-Host '== Backend: tests + jar ==' -ForegroundColor Cyan
Push-Location "$raiz/backend"
mvn -B clean package
Pop-Location

Write-Host ''
Write-Host 'Listo. Prueba local:' -ForegroundColor Green
Write-Host '  java -jar backend/target/mesa2-1.0.0.jar --spring.profiles.active=local'
Write-Host '  http://localhost:8080'
