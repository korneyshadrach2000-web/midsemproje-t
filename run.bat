@echo off
REM Student Records Manager - run with Maven (requires Maven and Java 11+)
cd /d "%~dp0"
if exist target\student-records-manager-1.0.0-all.jar (
    java -jar target\student-records-manager-1.0.0-all.jar
) else (
    echo Building with Maven...
    call mvn -q package -DskipTests
    if exist target\student-records-manager-1.0.0-all.jar (
        java -jar target\student-records-manager-1.0.0-all.jar
    ) else (
        echo Run with: mvn exec:java
        call mvn exec:java
    )
)
if errorlevel 1 pause
