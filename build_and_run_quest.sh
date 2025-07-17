#!/usr/bin/env bash

# Build and Run Quest Script
# Performs incremental build, install, and launch on Quest VR headset

echo "Building Quest APK (incremental)..."
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat assembleQuestDebug"

if [ $? -eq 0 ]; then
    echo "Build successful. Installing on Quest..."
    adb -s 192.168.1.194:5555 install -r "C:\Users\joepaley\AndroidStudioProjects\VideoPoker\app\build\outputs\apk\quest\debug\app-quest-debug.apk"
    
    if [ $? -eq 0 ]; then
        echo "Installation successful. Launching app..."
        adb -s 192.168.1.194:5555 shell am start -n com.hackathon.spatialvideopoker/.ImmersiveActivity
        echo "Quest app launched successfully!"
    else
        echo "Installation failed!"
        exit 1
    fi
else
    echo "Build failed!"
    exit 1
fi