# Script automatico para compilar e instalar NieR: Automata (0100B8E016F76000) con icono, controles y sin perder los 11 GB de RomFS
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$parentDir = Split-Path -Parent $repoRoot
$dmDir = Join-Path $repoRoot "nxnative\dm"
$bpy = Join-Path $dmDir "android\build.py"
$mkapk = Join-Path $dmDir "android\apk\mkapk.py"
$outDir = Join-Path $repoRoot "build_nier"
$apkOut = Join-Path $repoRoot "NieRAutomata.apk"

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

# 0. Eliminar perfil dummy de dm/games si existia (para no bloquear el GUI de nx2android)
$dummyGameDir = Join-Path $dmDir "games\0100B8E016F76000"
if (Test-Path $dummyGameDir) {
    Remove-Item -Recurse -Force $dummyGameDir
}

# 1. Buscar el game.tsv real generado por porter (0100B8E016F76000_build/profile/0100B8E016F76000/game.tsv) o crear uno en build_nier/profile
$realProfile = Get-ChildItem -Path $parentDir, "$env:USERPROFILE\OneDrive\Escritorio", "$env:USERPROFILE\Desktop", "$env:USERPROFILE\Downloads" -Filter "game.tsv" -Recurse -ErrorAction SilentlyContinue |
    Where-Object { $_.DirectoryName -match "0100B8E016F76000" -and $_.FullName -notmatch "nxnative\\dm\\games" -and $_.FullName -notmatch "build_nier" } |
    Select-Object -First 1

$profRoot = Join-Path $outDir "profile"
$profGameDir = Join-Path $profRoot "0100B8E016F76000"
New-Item -ItemType Directory -Force -Path $profGameDir | Out-Null
$gameTsv = Join-Path $profGameDir "game.tsv"

if ($realProfile) {
    Write-Host ">> Usando perfil real encontrado en: $($realProfile.FullName)" -ForegroundColor Green
    Copy-Item $realProfile.FullName $gameTsv -Force
    $tsvText = Get-Content $gameTsv -Raw
    if ($tsvText -notmatch "(?m)^render_cond\t") { $tsvText += "`nrender_cond`ton`n" }
    if ($tsvText -notmatch "(?m)^tex_flush_upload\t") { $tsvText += "tex_flush_upload`ton`n" }
    [System.IO.File]::WriteAllText($gameTsv, $tsvText)
} else {
    $tsvLines = @(
        "# Game profile of NieR:Automata The End of YoRHa Edition (0100B8E016F76000)",
        "title_id`t0100B8E016F76000",
        "program_id`t0100B8E016F76000",
        "name`tNieR:Automata The End of YoRHa Edition",
        "ipa`tNieRAutomata.ipa",
        "app_category`tpublic.app-category.action-games",
        "pad_layout`tfull",
        "build`tv0`t1.0.2`tmain=00000000000000000000000000000000`tsdk=00000000000000000000000000000000",
        "package`tv0",
        "shader_versions`tv0",
        "modules`tmain subsdk0 sdk",
        "romfs_marker`tshader.dat",
        "zip_store`t.cpk:.usm:.bnk:.pck:.dat",
        "mem_reserve`t0x140000000",
        "mem_alias`t0xB8000000",
        "mem_stack`t0x10000000",
        "app_pool`t0xCD500000",
        "sys_resource`t0x1000000",
        "mem_used`t0x7400000",
        "render_cond`ton",
        "tex_flush_upload`ton",
        "shader_groups`t/shader=core",
        "shader_note`tNieR:Automata shaders"
    )
    [System.IO.File]::WriteAllLines($gameTsv, $tsvLines)
}
$env:DM_GAMES_DIR = $profRoot

# 2. Extraer el icono original de ..\NieRAutomata.apk (si existe) para que el APK tenga su icono oficial
$iconPath = Join-Path $outDir "icon.png"
$origApk = Join-Path $parentDir "NieRAutomata.apk"
if ((Test-Path $origApk) -and -not (Test-Path $iconPath)) {
    python -c "import zipfile, sys; z=zipfile.ZipFile(sys.argv[1]); open(sys.argv[2],'wb').write(z.read('res/drawable-nodpi-v4/icon.png'))" $origApk $iconPath 2>$null
}

# 3. Compilar libdmhost.so
$ndkDir = Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\ndk" -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
if (-not $ndkDir) {
    $ndkDir = Get-ChildItem "$env:APPDATA\nx2android\toolchains" -Recurse -Directory -Filter "android-ndk*" -ErrorAction SilentlyContinue | Select-Object -First 1
}
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

# 4. Empaquetar NieRAutomata.apk con icono + controles tactiles (ergo theme) + alineacion 16 KB
Write-Host ">> 2. Empaquetando y firmando NieRAutomata.apk con icono y controles..." -ForegroundColor Cyan
$ergoDir = Join-Path $dmDir "android\themes\ergo"
$mkArgs = @(
    $mkapk,
    $soPath,
    $apkOut,
    "--package", "org.nx2a.nierautomata",
    "--label", "NieR: Automata",
    "--lib", "dmhost",
    "--landscape",
    "--asset", "overlay/theme.tsv=$(Join-Path $ergoDir 'theme.tsv')",
    "--asset", "overlay/phone.png=$(Join-Path $ergoDir 'phone.png')",
    "--asset", "overlay/tablet.png=$(Join-Path $ergoDir 'tablet.png')"
)
if (Test-Path $iconPath) {
    $mkArgs += @("--icon", $iconPath)
}
$nxKey = Join-Path "$env:APPDATA\nx2android" "signing-key.json"
if (Test-Path $nxKey) {
    $mkArgs += $nxKey
}
python @mkArgs
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $apkOut)) {
    throw "Error al generar NieRAutomata.apk"
}

# 5. Resguardar los 11 GB de RomFS en el telefono e instalar APK
Write-Host ">> 3. Resguardando los 11 GB de RomFS y shaders en el telefono..." -ForegroundColor Cyan
adb shell "if [ -d /sdcard/Android/data/org.nx2a.nierautomata/files/RomFS ]; then rm -rf /sdcard/Android/data/nier_safe_backup && mv /sdcard/Android/data/org.nx2a.nierautomata /sdcard/Android/data/nier_safe_backup; fi"
adb shell "cmd package uninstall org.nx2a.nierautomata >/dev/null 2>&1 || true"

Write-Host ">> 4. Instalando NieRAutomata.apk limpio..." -ForegroundColor Cyan
adb install -r $apkOut
if ($LASTEXITCODE -ne 0) {
    throw "Error al instalar NieRAutomata.apk por ADB"
}

Write-Host ">> 5. Inicializando carpeta con permisos del nuevo UID y restaurando los 11 GB..." -ForegroundColor Cyan
adb shell monkey -p org.nx2a.nierautomata -c android.intent.category.LAUNCHER 1 | Out-Null
Start-Sleep -Seconds 3
adb shell am force-stop org.nx2a.nierautomata
adb shell "mkdir -p /sdcard/Android/data/org.nx2a.nierautomata/files"

adb shell "if [ -d /sdcard/Android/data/nier_safe_backup/files ]; then mv /sdcard/Android/data/nier_safe_backup/files/* /sdcard/Android/data/org.nx2a.nierautomata/files/ && rm -rf /sdcard/Android/data/nier_safe_backup; fi"
adb shell "if [ -d /sdcard/Android/data/org.nx2a.nierautomata_backup/files ]; then mv /sdcard/Android/data/org.nx2a.nierautomata_backup/files/* /sdcard/Android/data/org.nx2a.nierautomata/files/ && rm -rf /sdcard/Android/data/org.nx2a.nierautomata_backup; fi"

adb shell "printf 'DM_RENDER_COND=1\nDM_TEX_FLUSH_UPLOAD=1\n' > /sdcard/Android/data/org.nx2a.nierautomata/files/env.txt"
adb shell rm -f /sdcard/Android/data/org.nx2a.nierautomata/files/log.txt

Write-Host ">> Contenido restaurado en /sdcard/Android/data/org.nx2a.nierautomata/files/:" -ForegroundColor Green
adb shell ls -lh /sdcard/Android/data/org.nx2a.nierautomata/files/
Write-Host ">> ¡LISTO! Ya puedes abrir NieR: Automata en tu telefono." -ForegroundColor Green
