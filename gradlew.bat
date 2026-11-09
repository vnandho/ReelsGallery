@echo off
setlocal
set GRADLE_VERSION=9.7.0
set BASE=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin\reels-gallery
set DIST=%BASE%\gradle-%GRADLE_VERSION%
set GRADLE=%DIST%\bin\gradle.bat
if not exist "%GRADLE%" (
  if not exist "%BASE%" mkdir "%BASE%"
  echo Downloading official Gradle %GRADLE_VERSION% distribution...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%BASE%\gradle.zip'; Expand-Archive -Path '%BASE%\gradle.zip' -DestinationPath '%BASE%' -Force; Remove-Item '%BASE%\gradle.zip'"
  if errorlevel 1 exit /b 1
)
call "%GRADLE%" %*
