# TA Recruitment System Architecture (Text-File Version)

## 1. Architecture style
- Stand-alone Java application
- Layered design: `module service -> repository -> text file storage`
- Storage uses CSV files only (`users.csv`, `positions.csv`, `applications.csv`)

## 2. Module mapping (aligned with `limits.md`)

### Module 1: TA-side Authentication (`TA-01`, `TA-02`)
- `auth/AuthService.java`
- `auth/SessionManager.java`
- Responsibilities:
  - TA/MO account registration
  - Login
  - Session token maintenance

### Module 2: TA-side Profile (`TA-03`, `TA-04`)
- `profile/ProfileService.java`
- Responsibilities:
  - Edit TA profile
  - Upload/replace CV path

### Module 3: TA-side Job (`TA-05`, `TA-06`)
- `ta_jobs/TAJobService.java`
- Responsibilities:
  - View open positions
  - Apply for positions
  - Duplicate application prevention
  - Deadline check

### Module 4: MO-side Publishing (`MO-01`, `MO-02`, `MO-03`)
- `mo_publish/MOPublishService.java`
- `Home.java` (Swing UI entry integrated for MO publishing flow)
- Responsibilities:
  - Publish positions
  - Update deadlines
  - Close positions

### Module 5: MO-side Review (`MO-04`, `MO-05`)
- `mo_review/MOReviewService.java`
- Responsibilities:
  - View applications for owned positions
  - Update application status

### Module 6: Admin + Foundation (`ADM-01`, `ADM-02`, `SYS-01`, `SYS-02`)
- `admin_system/AdminService.java`
- `admin_system/foundation/*`
- `admin_system/repository/*`
- Responsibilities:
  - Approve registrations
  - Enable/disable users
  - Reset passwords
  - Validation, hashing, storage utilities

## 3. Data files
- `TA_Recruitment_software/data/users.csv`
- `TA_Recruitment_software/data/positions.csv`
- `TA_Recruitment_software/data/applications.csv`

These files are auto-created at first run.

## 4. Entry point
- `ConsoleMain.java`
- Demonstrates TA/MO/Admin workflows in one executable CLI.
