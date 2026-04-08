# Software-Engineering\_Group70

|GitHub Username|QMID|
|-|-|
|xingchengkong|231225834|
|xsy1215-art  |231225801|
|Gelinghan-bot|231225915|
|DavidADP666  |231225856|
|Sun Haixin|231225823|
|LirPan       |231225890|
###### 

## Quick Start (Text-File Java Version)

### 1. Compile

```powershell
$files = Get-ChildItem -Path TA_Recruitment_software -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 $files
```

### 2. Run console system

```powershell
java TA_Recruitment_software.ConsoleMain
```

### 3. Default admin account
- account: `admin`
- password: `Admin@123`

### 4. Architecture
- architecture design file: `TA_Recruitment_software/ARCHITECTURE.md`
- storage files (auto-generated): `TA_Recruitment_software/data/*.csv`

