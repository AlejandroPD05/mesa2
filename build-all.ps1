# ===========================================================================
# Mesa 2.0 — build local de punta a punta: SPA + tests + jar único con la SPA
# dentro (el mismo resultado que produce el Dockerfile).
#
#   Uso:  ./build-all.ps1   (o `npm run build` en la raíz)
# ===========================================================================
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path

# npm 12: cuando el proceso padre (p. ej. `npm run`) propaga allow-scripts, los
# `npm ci` de proyecto fallan con EALLOWSCRIPTS. Se limpia antes de nada.
Remove-Item Env:\npm_config_allow_scripts -ErrorAction SilentlyContinue

function Assert-UltimoComando([string]$nombre) {
    if ($LASTEXITCODE -ne 0) {
        throw "«$nombre» terminó con código $LASTEXITCODE"
    }
}

Write-Host '== Frontend: dependencias ==' -ForegroundColor Cyan
Push-Location "$raiz/frontend"
npm ci
Assert-UltimoComando 'npm ci'

Write-Host '== Frontend: lint + tests ==' -ForegroundColor Cyan
npm run lint
Assert-UltimoComando 'npm run lint'
npm test
Assert-UltimoComando 'npm test'

Write-Host '== Frontend: compilación ==' -ForegroundColor Cyan
npm run build
Assert-UltimoComando 'npm run build'
Pop-Location

Write-Host '== Copiando la SPA al backend ==' -ForegroundColor Cyan
$destino = "$raiz/backend/src/main/resources/static"
New-Item -ItemType Directory -Force -Path $destino | Out-Null
Copy-Item -Recurse -Force "$raiz/frontend/dist/*" $destino

Write-Host '== Backend: tests + jar ==' -ForegroundColor Cyan
Push-Location "$raiz/backend"
mvn -B clean package
Assert-UltimoComando 'mvn package'
Pop-Location

Write-Host ''
Write-Host 'Listo. Prueba local:' -ForegroundColor Green
Write-Host '  java -jar backend/target/mesa2-1.0.0.jar --spring.profiles.active=local'
Write-Host '  http://localhost:8080'
