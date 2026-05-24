@echo off
chcp 65001 >nul
echo.
echo ========================================
echo   TA Recruitment System - Run All Tests
echo ========================================
echo.

cd /d "%~dp0"
cd ..

set ROOT_DIR=%CD%
set SOURCE_DIR=%ROOT_DIR%\TA_Recruitment_software
set TEST_DIR=%ROOT_DIR%\test
set OUT_DIR=%ROOT_DIR%\test_out

rd /s /q %OUT_DIR% 2>nul
mkdir %OUT_DIR%

echo ========================================
echo   Compiling source files...
echo ========================================
echo.

REM Compile foundation
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%SOURCE_DIR%" "%SOURCE_DIR%\admin_system\foundation\AppException.java" "%SOURCE_DIR%\admin_system\foundation\ValidationUtil.java"

REM Compile model enums first
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%SOURCE_DIR%" "%SOURCE_DIR%\admin_system\model\Role.java" "%SOURCE_DIR%\admin_system\model\ApprovalStatus.java" "%SOURCE_DIR%\admin_system\model\PositionStatus.java" "%SOURCE_DIR%\admin_system\model\ApplicationStatus.java"

REM Compile model classes
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%SOURCE_DIR%" -cp "%OUT_DIR%" "%SOURCE_DIR%\admin_system\model\User.java" "%SOURCE_DIR%\admin_system\model\Position.java" "%SOURCE_DIR%\admin_system\model\Application.java"

REM Compile ta_jobs classes
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%SOURCE_DIR%" -cp "%OUT_DIR%" "%SOURCE_DIR%\ta_jobs\JobMatchResult.java"

REM Compile mo_review model classes
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%SOURCE_DIR%" "%SOURCE_DIR%\mo_review\InterviewNote.java" "%SOURCE_DIR%\mo_review\MoInvitation.java"

echo ========================================
echo   Compiling test files...
echo ========================================
echo.

javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%SOURCE_DIR%" -cp "%OUT_DIR%" "%TEST_DIR%\ValidationUtilTest.java"
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%TEST_DIR%" -cp "%OUT_DIR%" "%TEST_DIR%\admin_system\model\UserTest.java"
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%TEST_DIR%" -cp "%OUT_DIR%" "%TEST_DIR%\admin_system\model\PositionTest.java"
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%TEST_DIR%" -cp "%OUT_DIR%" "%TEST_DIR%\admin_system\model\ApplicationTest.java"
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%TEST_DIR%" -cp "%OUT_DIR%" "%TEST_DIR%\mo\InterviewNoteTest.java"
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%TEST_DIR%" -cp "%OUT_DIR%" "%TEST_DIR%\mo\MoInvitationTest.java"
javac -d %OUT_DIR% -encoding UTF-8 -sourcepath "%TEST_DIR%" -cp "%OUT_DIR%" "%TEST_DIR%\ta_jobs\JobMatchResultTest.java"

echo ========================================
echo   Running tests...
echo ========================================
echo.

echo === ValidationUtil Tests ===
java -cp "%OUT_DIR%" -ea ValidationUtilTest
echo.

echo === User Model Tests ===
java -cp "%OUT_DIR%" -ea admin_system.model.UserTest
echo.

echo === Position Model Tests ===
java -cp "%OUT_DIR%" -ea admin_system.model.PositionTest
echo.

echo === Application Model Tests ===
java -cp "%OUT_DIR%" -ea admin_system.model.ApplicationTest
echo.

echo === MO Tests ===
java -cp "%OUT_DIR%" -ea mo.InterviewNoteTest
echo.

java -cp "%OUT_DIR%" -ea mo.MoInvitationTest
echo.

echo === TA Jobs Tests ===
java -cp "%OUT_DIR%" -ea ta_jobs.JobMatchResultTest
echo.

echo ========================================
echo   All tests completed!
echo ========================================
echo.

rd /s /q %OUT_DIR% 2>nul
pause
