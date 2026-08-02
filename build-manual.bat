@echo off
REM Manual build script for Villager Behavior Mod
REM This script compiles the Java files and creates a JAR without using Gradle

echo ========================================
echo Manual Build Script for Villager Mod
echo ========================================
echo.

REM Set up paths
set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src\main\java
set RES_DIR=%PROJECT_DIR%src\main\resources
set BUILD_DIR=%PROJECT_DIR%build\classes
set JAR_FILE=%PROJECT_DIR%build\libs\villager-behavior-mod-1.0.0.jar

REM Create build directories
echo Creating build directories...
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if not exist "%PROJECT_DIR%build\libs" mkdir "%PROJECT_DIR%build\libs"

REM Note: This script requires Minecraft Forge dependencies to be present
REM You need to download the Forge MDK and extract the forge-1.16.5-36.2.34.jar
REM Place it in the libs folder or adjust the classpath below

echo.
echo ========================================
echo IMPORTANT: This manual build requires Forge dependencies
echo ========================================
echo.
echo To build this mod manually, you need:
echo 1. Download Forge MDK 1.16.5-36.2.34 from https://files.minecraftforge.net/
echo 2. Extract forge-1.16.5-36.2.34.jar to a temporary location
echo 3. Copy the following files to the libs folder:
echo    - forge-1.16.5-36.2.34-universal.jar
echo    - All other JAR files from the Forge MDK
echo 4. Update the CLASSPATH in this script to include those files
echo.
echo Alternatively, build the project in an environment with working Java/SSL.
echo.

REM Check if Java is available
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: javac not found in PATH
    echo Please install Java JDK and add it to your PATH
    pause
    exit /b 1
)

echo Java compiler found.
echo.

REM Compile Java files (this will fail without Forge dependencies)
echo Attempting to compile Java files...
echo.

javac -d "%BUILD_DIR%" -sourcepath "%SRC_DIR%" "%SRC_DIR%\com\villagermod\*.java"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed. This is expected without Forge dependencies.
    echo.
    echo To properly build this mod, use one of these methods:
    echo.
    echo 1. Build in an environment with working Java and SSL certificates
    echo 2. Use Docker container with proper Java environment
    echo 3. Copy the project to a machine with working Java/SSL
    echo.
    pause
    exit /b 1
)

echo Compilation successful!
echo.

REM Create JAR file
echo Creating JAR file...
echo.

jar -cvf "%JAR_FILE%" -C "%BUILD_DIR%" . -C "%RES_DIR%" .

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to create JAR file
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build successful!
echo ========================================
echo.
echo JAR file created at: %JAR_FILE%
echo.
echo You can now install this JAR in your Minecraft mods folder.
echo.
pause
