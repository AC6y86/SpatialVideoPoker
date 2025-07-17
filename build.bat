@echo off
echo Building VideoPoker Android App...

:: Set JAVA_HOME
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr

:: Navigate to project directory
cd /d "C:\Users\joepaley\AndroidStudioProjects\VideoPoker"

:: Clean previous build
echo Cleaning previous build...
call gradlew.bat clean

:: Build debug APKs for both platforms
echo Building debug APKs...
call gradlew.bat assembleDebug

echo Build complete!
echo APK locations:
echo Mobile: app\build\outputs\apk\mobile\debug\app-mobile-debug.apk
echo Quest:  app\build\outputs\apk\quest\debug\app-quest-debug.apk