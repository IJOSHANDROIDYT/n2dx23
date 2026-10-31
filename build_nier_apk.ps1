# Script automatico para compilar e instalar NieR: Automata (0100B8E016F76000) sin perder los 11 GB de RomFS
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$dmDir = Join-Path $repoRoot "nxnative\dm"
$bpy = Join-Path $dmDir "android\build.py"
$mkapk = Join-Path $dmDir "android\apk\mkapk.py"
$outDir = Join-Path $repoRoot "build_nier"
$apkOut = Join-Path $repoRoot "NieRAutomata.apk"

# Asegurar que exista el perfil 0100B8E016F76000/game.tsv
$gameDir = Join-Path $dmDir "games\0100B8E016F76000"
$gameTsv = Join-Path $gameDir "game.tsv"
if (-not (Test-Path $gameTsv)) {
    New-Item -ItemType Directory -Force -Path $gameDir | Out-Null
    $tsvLines = @(
        "# NieR:Automata The End of YoRHa Edition",
        "title`tNieR:Automata The End of YoRHa Edition",
        "modules`tmain`tsubsdk0`tsdk",
        "render_cond`ton",
        "tex_flush_upload`ton"
    )
    [System.IO.File]::WriteAllLines($gameTsv, $tsvLines)
}

$ndkDir = Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\ndk" -Directory | Sort-Object Name -Descending | Select-Object -First 1
if (-not $ndkDir) {
    throw "No se encontro Android NDK en $env:LOCALAPPDATA\Android\Sdk\ndk"
}

Write-Host ">> 1. Compilando libdmhost.so con NDK $($ndkDir.Name)..." -ForegroundColor Cyan
python $bpy --game 0100B8E016F76000 --ndk $ndkDir.FullName --lib --out $outDir
if ($LASTEXITCODE -ne 0) {
    throw "Error al compilar libdmhost.so"
}

$soPath = Join-Path $outDir "libdmhost.so"
if (-not (Test-Path $soPath)) {
    throw "No se genero $soPath"
}

Write-Host ">> 2. Empaquetando y firmando NieRAutomata.apk (16 KB aligned)..." -ForegroundColor Cyan
python $mkapk $soPath $apkOut --package org.nx2a.nierautomata --label "NieR: Automata" --lib dmhost --landscape
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $apkOut)) {
    throw "Error al generar NieRAutomata.apk"
}

Write-Host ">> 3. Resguardando los 11 GB de RomFS y shaders en el telefono..." -ForegroundColor Cyan
adb shell "if [ -d /sdcard/Android/data/org.nx2a.nierautomata/files/RomFS ]; then rm -rf /sdcard/Android/data/nier_safe_backup && mv /sdcard/Android/data/org.nx2a.nierautomata /sdcard/Android/data/nier_safe_backup; fi"
adb shell "cmd package uninstall org.nx2a.nierautomata >/dev/null 2>&1 || true"

Write-Host ">> 4. Instalando NieRAutomata.apk limpio..." -ForegroundColor Cyan
adb install -r $apkOut
if ($LASTEXITCODE -ne 0) {
    throw "Error al instalar NieRAutomata.apk por ADB"
}

Write-Host ">> 5. Inicializando carpeta con permisos del nuevo UID y restaurando RomFS..." -ForegroundColor Cyan
adb shell monkey -p org.nx2a.nierautomata -c android.intent.category.LAUNCHER 1 | Out-Null
Start-Sleep -Seconds 2
adb shell am force-stop org.nx2a.nierautomata

adb shell "mkdir -p /sdcard/Android/data/org.nx2a.nierautomata/files"
adb shell "if [ -d /sdcard/Android/data/nier_safe_backup/files ]; then mv /sdcard/Android/data/nier_safe_backup/files/* /sdcard/Android/data/org.nx2a.nierautomata/files/ && rm -rf /sdcard/Android/data/nier_safe_backup; fi"
adb shell "if [ -d /sdcard/Android/data/org.nx2a.nierautomata_backup/files ]; then mv /sdcard/Android/data/org.nx2a.nierautomata_backup/files/* /sdcard/Android/data/org.nx2a.nierautomata/files/ && rm -rf /sdcard/Android/data/org.nx2a.nierautomata_backup; fi"

adb shell "printf 'DM_RENDER_COND=1\nDM_TEX_FLUSH_UPLOAD=1\n' > /sdcard/Android/data/org.nx2a.nierautomata/files/env.txt"
adb shell rm -f /sdcard/Android/data/org.nx2a.nierautomata/files/log.txt

Write-Host ">> Contenido de /sdcard/Android/data/org.nx2a.nierautomata/files/:" -ForegroundColor Green
adb shell ls -lh /sdcard/Android/data/org.nx2a.nierautomata/files/
Write-Host ">> ¡LISTO! Ya puedes abrir NieR: Automata en tu telefono." -ForegroundColor Green
