@echo off
echo ========================================================
echo   KOMPILASI PROYEK RENTARA MANAGEMENT SYSTEM (PBO)
echo ========================================================
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -cp "lib/*" -d bin @sources.txt
if %ERRORLEVEL% EQU 0 (
    echo [SUKSES] Seluruh berkas Java berhasil dikompilasi ke folder bin!
) else (
    echo [ERROR] Kompilasi gagal! Periksa pesan kesalahan di atas.
)
del sources.txt
pause
