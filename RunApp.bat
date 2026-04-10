@echo off
REM 切换到正确的代码目录
cd /d "%~dp0\TA_Recruitment_software"

REM 编译所有的 .java 文件并将生成的 .class 文件放入正确的 out 目录
echo Compiling Java source files...
dir /s /B *.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt

REM 运行最新的系统
echo Starting Application...
java -cp out TA_Recruitment_software.Home2
