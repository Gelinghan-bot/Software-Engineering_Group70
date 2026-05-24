# Software-Engineering_Group70

|GitHub Username|QMID|
|-|-|
|xingchengkong|231225834|
|xsy1215-art  |231225801|
|Gelinghan-bot|231225915|
|DavidADP666  |231225856|
|Sun Haixin|231225823|
|LirPan       |231225890|

---

# JobHere - BUPT International School TA Recruitment System

> A lightweight Java Swing desktop application for streamlining Teaching Assistant recruitment workflow.

## Table of Contents
- [Project Overview](#project-overview)
- [System Requirements](#system-requirements)
- [Quick Start](#quick-start)
- [Default Accounts](#default-accounts)
- [User Guide](#user-guide)
- [AI Features](#ai-features)
- [Data Storage](#data-storage)
- [Project Structure](#project-structure)
- [FAQ](#faq)

---

## Project Overview

**JobHere** is a comprehensive Teaching Assistant (TA) recruitment system developed for BUPT International School. It replaces the traditional paper/Excel-based recruitment process with a modern, efficient digital platform.

### Key Features
- **Role-based access**: TA (Teaching Assistant), MO (Module Organizer), ADMIN
- **Job posting & application**: MO publishes positions, TA browses and applies
- **Application review**: MO reviews candidates, manages interview notes
- **AI-powered tools**: AI job description generation, AI candidate comparison
- **Forum system**: Community discussion, polls, notifications
- **CV management**: Upload and manage curriculum vitae
- **Workload tracking**: Admin monitors TA workload distribution

---

## System Requirements

- **Java Development Kit**: JDK 8 or higher
- **Operating System**: Windows 10/11, macOS, or Linux
- **Memory**: Minimum 512MB RAM recommended
- **Disk Space**: ~10MB for the application + data files

---

## Quick Start

### Method 1: One-Click Launch (Windows)

Simply double-click the `RunApp.bat` file in the project root directory. The batch script will automatically compile and launch the application.

### Method 2: Command Line (Cross-Platform)

#### Step 1: Compile All Java Files

**Windows (PowerShell):**
```powershell
$files = Get-ChildItem -Path TA_Recruitment_software -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 $files
```

**macOS/Linux (Bash):**
```bash
find TA_Recruitment_software -name "*.java" | xargs javac -encoding UTF-8
```

#### Step 2: Run the Application

**Launch GUI version (recommended):**
```bash
java -cp . TA_Recruitment_software.Home2
```

**Launch Console version (legacy):**
```bash
java -cp . TA_Recruitment_software.ConsoleMain
```

---

## Default Accounts

The system comes with pre-configured test accounts for demonstration:

| Role | Account | Password | Description |
|------|---------|----------|-------------|
| **ADMIN** | `admin` | `Admin@123` | System administrator |
| **TA** | `ta` | `Ta@12345` | Teaching Assistant applicant |
| **MO** | `mo` | `Mo@12345` | Module Organizer |

> **Note**: You can register new accounts through the "Register" button on the login screen.

---

## User Guide

### TA (Teaching Assistant) Workflow

1. **Register/Login**: Create an account or log in with existing credentials
2. **Complete Profile**: 
   - Navigate to `PERSONAL` → `My Resume (Update Profile)`
   - Fill in personal information, major, skills, and upload CV
3. **Browse Jobs**: 
   - Click `JOBS` to view available TA positions
   - Use filters (Grade, Major, Category) to narrow results
   - Search by course name
4. **Apply for Positions**:
   - Click on a job card to view details
   - Click "Apply Now" to submit application
   - Track application status in `PERSONAL` → `My Applications`
5. **Check Notifications**:
   - `PERSONAL` → `Notifications` shows status updates
   - Double-click any notification to view details
6. **Participate in Forum**:
   - `FORUM` → Browse topics, post new topics, comment, like, favorite
   - `My Dashboard` → View your messages and activities

### MO (Module Organizer) Workflow

1. **Publish Positions**:
   - `JOBS` → `Post a Job (MO)`
   - **AI Quick Publish**: Select grade/major/category, click "AI-Autofill Publish" for auto-generated job description
   - Manually fill in job title, description, requirements, deadline, headcount
2. **Manage Positions**:
   - `JOBS` → `Manage My Positions (MO)`
   - Edit, close, or delete published positions
3. **Review Applications**:
   - `JOBS` → `Review Applications (MO)`
   - View all applicants for each position
   - **AI Compare**: Select multiple candidates, click "AI Compare Selected" for intelligent comparison
   - Update application status through the workflow: PENDING → SHORTLISTED → INTERVIEWED → OFFERED → HIRED
   - Add interview notes for each candidate
4. **View TA Profiles**:
   - `JOBS` → `View TA Profiles & CVs (MO)`
   - Browse applicant profiles and downloaded CVs

### ADMIN Workflow

1. **User Management**:
   - `ADMIN` → `Approve Registration`: Review and approve new user registrations
   - `ADMIN` → `Manage Users`: Search, enable/disable users, view details
2. **Position Management**:
   - View and manage all positions across the system
   - Override MO actions when necessary
3. **Forum Administration**:
   - `FORUM` → Access forum management features
   - `ADMIN` → `View Poll Results`: Monitor weekly poll results
4. **System Settings**:
   - `SETTINGS` → Configure system-wide options

---

## AI Features

### 1. AI Job Description Generator
- **Location**: MO → Post a Job → "AI-Autofill Publish" button
- **How it works**: Based on selected grade, major, and job type, the AI generates a professional job title, description, and requirements
- **API**: Uses Alibaba Cloud DashScope (Qwen Plus model)
- **Configuration**: Edit `data/ai-config.properties` to set your API key

### 2. AI Candidate Comparison
- **Location**: MO → Review Applications → Select multiple candidates → "AI Compare Selected"
- **How it works**: AI analyzes selected candidates' profiles against job requirements and provides a ranked comparison
- **Output**: Detailed analysis of strengths, weaknesses, and recommended ranking

### Configuring AI API Key
1. Open `data/ai-config.properties`
2. Replace `YOUR_API_KEY_HERE` with your actual Alibaba Cloud API key
3. Optionally change the model name (default: `qwen-plus`)

---

## Data Storage

All data is stored in plain text files under the `data/` directory:

| File | Description |
|------|-------------|
| `users.csv` | User accounts and profiles |
| `positions.csv` | Job positions |
| `applications.csv` | TA applications |
| `sessions.csv` | Login sessions |
| `topics.csv` | Forum topics |
| `comments.csv` | Forum comments |
| `poll_votes.csv` | Weekly poll votes |
| `ta_notifications.csv` | TA notifications |
| `interview_notes.csv` | MO interview notes |
| `ai-config.properties` | AI API configuration |
| `cv/` | Uploaded CV documents |

> **Note**: No database required. All data persists in CSV format for easy backup and inspection.

---

## Project Structure

```
Software-Engineering_Group70/
├── RunApp.bat                    # Windows one-click launcher
├── data/                         # Data storage (auto-generated)
│   ├── *.csv                     # CSV data files
│   ├── ai-config.properties      # AI API configuration
│   └── cv/                       # Uploaded CVs
├── TA_Recruitment_software/      # Source code
│   ├── Home2.java               # Main GUI entry point
│   ├── ConsoleMain.java         # Console version entry point
│   ├── ARCHITECTURE.md          # Architecture documentation
│   ├── admin_system/            # Admin module
│   ├── auth/                    # Authentication & session
│   ├── forum/                   # Forum module
│   ├── mo_publish/              # MO job publishing
│   ├── mo_review/               # MO application review
│   ├── profile/                 # User profile management
│   ├── ta_jobs/                 # TA job browsing & application
│   └── ui/                      # Shared UI components
└── First_Assessment_Resources/  # First assessment materials
```

---

## FAQ

### Q: The application doesn't start / shows compilation errors
**A:** Ensure you have JDK installed and `java`/`javac` commands are in your PATH. Run `java -version` to verify.

### Q: How do I change the AI API key?
**A:** Edit `data/ai-config.properties` and replace `YOUR_API_KEY_HERE` with your actual API key from Alibaba Cloud DashScope.

### Q: Can I use this without AI features?
**A:** Yes! All core features work without AI. AI is optional and enhances the experience.

### Q: How is data backed up?
**A:** Simply copy the entire `data/` folder. All data is in human-readable CSV format.

### Q: Can multiple users log in simultaneously?
**A:** The current version is single-instance. Each user should run their own instance.

### Q: What browsers are supported?
**A:** This is a desktop application, not a web application. No browser required.

## Team

This project was developed by Group 70 for the EBU6304 Software Engineering course at BUPT International School.

**Technologies**: Java SE, Swing, CSV File Storage, Alibaba Cloud AI API

---

### 3.3.1 Test Coverage Overview

We have developed **7 test files** covering **59 individual test cases** across **4 modules**:

| Module | Test File | Test Cases | Coverage Description |
|--------|-----------|------------|---------------------|
| **foundation** | ValidationUtilTest.java | 14 | Input validation (email, password, phone, date, string sanitisation) |
| **admin_system.model** | UserTest.java | 13 | User entity (13 properties: userId, role, accountId, fullName, studentId, email, phone, skills, approvalStatus, enabled, passwordHash, department, cvFilePath) |
| **admin_system.model** | PositionTest.java | 11 | Position entity (11 properties: positionId, jobTitle, jobType, grade, major, courseName, requirements, status, postedBy, createdAt, aiGenerated) |
| **admin_system.model** | ApplicationTest.java | 7 | Application entity (7 properties: applicationId, applicantUserId, positionId, status, appliedAt, cvFilePath, notes) |
| **mo** | InterviewNoteTest.java | 5 | Interview note entity (5 properties: noteId, applicationId, moUserId, noteContent, lastUpdatedAt) |
| **mo** | MoInvitationTest.java | 6 | MO invitation entity (6 properties: invitationId, moUserId, taUserId, positionId, message, createdAt) |
| **ta_jobs** | JobMatchResultTest.java | 3 | Job matching (Position, score boundary clamping 0-100, null/empty reason handling) |
| **Total** | | **59** | |

### 3.3.2 Specific Test Case Examples

#### Example 1: ValidationUtil - Email Validation

**Test File:** `ValidationUtilTest.java`

| Test Case | Input | Expected Result | Technique |
|-----------|-------|-----------------|-----------|
| Valid email | `"test@example.com"` | Returns `"test@example.com"` | Boundary value |
| Null email | `null` | Throws `AppException` | Error condition |
| Empty email | `""` | Throws `AppException` | Boundary value |
| Email with spaces | `"  test@example.com  "` | Returns `"test@example.com"` (trimmed) | String sanitisation |
| Invalid email format | `"not-an-email"` | Throws `AppException` | Error condition |

#### Example 1: ValidationUtil - Email Validation

**Test File:** `ValidationUtilTest.java`

| Test Case | Input | Expected Result | Technique |
|-----------|-------|-----------------|-----------|
| Valid email | `"test@example.com"` | Returns `"test@example.com"` | Boundary value |
| Null email | `null` | Throws `AppException` | Error condition |
| Empty email | `""` | Throws `AppException` | Boundary value |
| Email with spaces | `"  test@example.com  "` | Returns `"test@example.com"` (trimmed) | String sanitisation |
| Invalid email format | `"not-an-email"` | Throws `AppException` | Error condition |

**Code Snippet:**
```java
private static void testEmailValid() {
    try {
        String result = ValidationUtil.requireEmail("test@example.com");
        assert result.equals("test@example.com") : "Should return valid email";
        pass("testEmailValid");
    } catch (Exception e) {
        fail("testEmailValid", e);
    }
}
```

#### Example 2: User Model - Complete Property Testing

**Test File:** `UserTest.java`

Tests all 13 properties of the `User` entity, ensuring getter/setter pairs work correctly:

```java
private static void testRole() {
    try {
        User user = new User();
        user.setRole(Role.TA);
        assert user.getRole() == Role.TA : "Role should be TA";
        pass("testRole");
    } catch (Exception e) {
        fail("testRole", e);
    }
}

private static void testApprovalStatus() {
    try {
        User user = new User();
        user.setApprovalStatus(ApprovalStatus.PENDING);
        assert user.getApprovalStatus() == ApprovalStatus.PENDING : "ApprovalStatus should be PENDING";
        pass("testApprovalStatus");
    } catch (Exception e) {
        fail("testApprovalStatus", e);
    }
}
```

#### Example 3: JobMatchResult - Score Boundary Clamping

**Test File:** `JobMatchResultTest.java`

Tests the score clamping logic that ensures match scores are always within the valid range [0, 100]:

```java
private static void testScoreClamping() {
    try {
        Position pos = new Position();
        // Score > 100 should clamp to 100
        JobMatchResult high = new JobMatchResult(pos, 150, "Over 100");
        assert high.getScorePercent() == 100 : "Score should be clamped to 100";
        // Score < 0 should clamp to 0
        JobMatchResult low = new JobMatchResult(pos, -20, "Below 0");
        assert low.getScorePercent() == 0 : "Score should be clamped to 0";
        pass("testScoreClamping");
    } catch (Exception e) {
        fail("testScoreClamping", e);
    }
}
```

#### Example 4: ValidationUtil - Password Strength

**Test File:** `ValidationUtilTest.java`

Tests password strength validation with multiple criteria:

| Test Case | Input | Expected |
|-----------|-------|----------|
| Valid password | `"StrongPass1!"` | Passes validation |
| Too short (5 chars) | `"Abc1!"` | Fails - minimum length 6 |
| Too short (7 chars) | `"Abc1!ef"` | Fails - minimum length 8 |
| No uppercase | `"password1!"` | Fails - requires uppercase |
| No digit | `"Password!"` | Fails - requires digit |
| No special char | `"Password1"` | Fails - requires special character |

### 3.3.3 Test Data Design Principles

1. **Happy Path**: Normal, expected inputs that should succeed
2. **Boundary Values**: Edge cases at the limits of valid input ranges
3. **Error Conditions**: Invalid inputs that should trigger exceptions or rejection
4. **Null Safety**: Explicit testing of null inputs for robustness
5. **String Trimming**: Verification that whitespace is properly handled
