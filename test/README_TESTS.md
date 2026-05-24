# Unit Tests Guide

## Overview

This project contains 7 unit test files covering the core data models and business classes, with a total of **59 test cases**:

| Module | Test File | Test Type | Count |
|--------|-----------|-----------|-------|
| **foundation** | ValidationUtilTest.java | Functional | 14 |
| **admin_system.model** | UserTest.java | Functional | 13 |
| **admin_system.model** | PositionTest.java | Functional | 11 |
| **admin_system.model** | ApplicationTest.java | Functional | 7 |
| **mo** | InterviewNoteTest.java | Functional | 5 |
| **mo** | MoInvitationTest.java | Functional | 6 |
| **ta_jobs** | JobMatchResultTest.java | Functional | 3 |
| **Total** | | | **59 tests** |

## File Structure

```
test/
├── RunAllTests.bat                      ← Run all tests with one click
├── ValidationUtilTest.java              ← Input validation tests
├── admin_system/
│   └── model/
│       ├── UserTest.java                ← User model tests
│       ├── PositionTest.java            ← Position model tests
│       └── ApplicationTest.java         ← Application model tests
├── mo/
│   ├── InterviewNoteTest.java           ← Interview note model tests
│   └── MoInvitationTest.java            ← MO invitation model tests
├── ta_jobs/
│   └── JobMatchResultTest.java          ← Job match result tests
└── README_TESTS.md                      ← This documentation
```

## Running Tests

### Quick Run (Recommended)

Double-click `RunAllTests.bat` or run in command line:

```bash
cd test
RunAllTests.bat
```

The script will automatically compile source and test files, run all tests, and display results.

### Running a Single Test Manually

```bash
cd test
javac -d ..\test_out -encoding UTF-8 -sourcepath "..\TA_Recruitment_software" -cp "..\TA_Recruitment_software" ValidationUtilTest.java
java -cp "..\test_out;..\TA_Recruitment_software" -ea ValidationUtilTest
```

**Note:** Assertions must be enabled (`-ea` flag)

## Test Coverage

### foundation - Utility Classes
| Class | Coverage |
|-------|----------|
| ValidationUtil | Email, password, phone, date validation; string sanitization |

### admin_system.model - Data Models
| Class | Coverage |
|-------|----------|
| User | All user properties (userId, role, accountId, fullName, etc. - 13 properties) |
| Position | All position properties (positionId, jobTitle, jobType, etc. - 11 properties) |
| Application | All application properties (applicationId, applicantUserId, etc. - 7 properties) |

### mo - MO Module Models
| Class | Coverage |
|-------|----------|
| InterviewNote | Interview note properties (noteId, applicationId, etc. - 5 properties) |
| MoInvitation | Invitation properties (invitationId, moUserId, etc. - 6 properties) |

### ta_jobs - TA Module
| Class | Coverage |
|-------|----------|
| JobMatchResult | Job match result (Position, score boundary clamping, null handling) |

## Example Output

### Successful Run
```
========================================
  TA Recruitment System - Run All Tests
========================================

=== ValidationUtil Tests ===
========================================
ValidationUtil Unit Tests
========================================

[PASS] requireNotBlank_Valid
[PASS] requireNotBlank_Null
[PASS] requireNotBlank_Empty
...

========================================
Test Summary
========================================
Total: 14
Passed: 14
Failed: 0
========================================

========================================
  All tests completed!
========================================
```

## IDE Notes

VS Code may show package declarations in red, as the `test/` directory is not configured as a source folder. **This does not affect compilation or execution** — RunAllTests.bat handles paths correctly.

To suppress the warnings, add the following to `.vscode/settings.json`:
```json
{
    "java.project.sourcePaths": [
        "TA_Recruitment_software",
        "test"
    ]
}
```

## Submission Notes

1. **Include the test directory**
   - Package the entire `test/` directory in the submitted ZIP file

2. **Verify all tests pass**
   - Run `RunAllTests.bat` before submission
   - Ensure all 59 tests pass successfully

3. **Document in the report**
   - List all test files
   - Describe the modules covered by testing
   - Include screenshots of test results

---
*Group70 - TA Recruitment System - 59 Unit Tests*
