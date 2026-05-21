@echo off
REM Compile all .java files from the source directory
cd /d "%~dp0\TA_Recruitment_software"
dir /s /B *.java > sources.txt
javac -encoding UTF-8 -d "..\out" @sources.txt
del sources.txt

REM Run from the project root so data\ and out\ paths match Mac/PyCharm
cd /d "%~dp0"
echo Starting Application...
java -cp out TA_Recruitment_software.Home2
