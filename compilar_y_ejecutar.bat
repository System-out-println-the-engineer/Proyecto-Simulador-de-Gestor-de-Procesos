@echo off
echo ======================================
echo  Simulador de Gestor de Procesos - UAT
echo ======================================
echo.

:: Crear directorio de salida
if not exist out mkdir out

:: Encontrar todos los archivos Java
if exist _sources.txt del _sources.txt
powershell -Command "(Get-ChildItem -Path src -Filter *.java -Recurse -File).FullName -replace '\\', '/' | ForEach-Object { '\"' + $_ + '\"' } | Out-File -Encoding ASCII _sources.txt"
javac -d out -encoding UTF-8 @_sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] La compilacion fallo.
    del _sources.txt
    pause
    exit /b 1
)

del _sources.txt
echo Compilacion exitosa.
echo.

:: Ejecutar
echo Ejecutando simulador...
echo.
java -cp out com.simulador.Main

pause
