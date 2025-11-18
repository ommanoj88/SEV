@echo off
cd /d "c:\Users\omman\Desktop\SEV\backend\evfleet-monolith"
"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests -q
pause
