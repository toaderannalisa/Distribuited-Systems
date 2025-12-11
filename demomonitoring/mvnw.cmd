@echo off
cd /d "%~dp0"
mvn clean install -DskipTests -f pom.xml
