# Kompilasi Proyek Rentara Management System
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  KOMPILASI PROYEK RENTARA MANAGEMENT SYSTEM (PBO)" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

cmd /c "dir /s /b src\*.java > sources.txt && javac -cp lib/* -d bin @sources.txt && del sources.txt"

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUKSES] Kompilasi berhasil! Berkas class tersedia di ./bin" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Kompilasi gagal." -ForegroundColor Red
}
