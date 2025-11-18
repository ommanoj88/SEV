@echo off
cd /d "%~dp0"
echo Running Spring Boot application with dev profile...
"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=dev
