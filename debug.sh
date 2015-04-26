ant debug; 
time adb uninstall com.frrahat.quransimple && 
time adb install bin/Quran-For-My-Android-debug.apk
echo LOGCAT STARTED...
adb logcat | grep "QURAN"
