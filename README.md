# 🦷 Sunrise Dental Clinic Management System

> **Cardiff Metropolitan University — CIS6003 Enterprise Systems Development**
> A 3-Tier Distributed Patient & Appointment Management System built with Java Servlets, MySQL, and Apache Tomcat.

---

## 📋 Table of Contents

- [Prerequisites](#-prerequisites)
- [Project Structure](#-project-structure)
- [Step 1 — Database Setup](#step-1--database-setup)
- [Step 2 — Configure Database Connection](#step-2--configure-database-connection)
- [Step 3 — Build the Project](#step-3--build-the-project)
- [Step 4 — Deploy & Run on Tomcat](#step-4--deploy--run-on-tomcat)
- [Step 5 — Access the Application](#step-5--access-the-application)
- [Running Tests](#-running-tests)
- [Stopping the Server](#-stopping-the-server)
- [Troubleshooting](#-troubleshooting)

---

## ✅ Prerequisites

Ensure the following are installed before proceeding:

| Tool | Version | Download |
|------|---------|----------|
| **Java JDK** | 21 or higher | [oracle.com/java](https://www.oracle.com/java/technologies/downloads/) |
| **Apache Maven** | 3.8+ | [maven.apache.org](https://maven.apache.org/download.cgi) |
| **Apache Tomcat** | 8.5+ | [tomcat.apache.org](https://tomcat.apache.org/download-80.cgi) |
| **MySQL Server** | 8.0+ | [mysql.com](https://dev.mysql.com/downloads/mysql/) |
| **XAMPP** *(optional)* | Any | [apachefriends.org](https://www.apachefriends.org/) — includes Tomcat + MySQL |

Verify your setup:

```bash
java -version        # Should show 21 or higher
mvn -version         # Should show 3.8+
mysql --version      # Should show 8.0+
```

---

## 📁 Project Structure

```
project/
├── src/
│   ├── main/
│   │   ├── java/com/sunrisedental/    # Java source (Servlets, DAOs, Services)
│   │   ├── resources/db/schema.sql   # Database schema & seed data
│   │   └── webapp/                   # HTML, CSS, JS frontend files
│   └── test/java/com/sunrisedental/  # JUnit 5 test classes
├── WEB-INF/
│   ├── application.properties        # Database connection config
│   ├── web.xml                       # Servlet & filter mappings
│   └── lib/                          # Runtime JARs (MySQL, HikariCP, Gson)
├── pom.xml                           # Maven build configuration
└── README.md
```

---

## Step 1 — Database Setup

### 1a. Start MySQL

**With XAMPP:** Open the XAMPP Control Panel and click **Start** next to **MySQL**.

**Without XAMPP (Windows):**
```bash
net start MySQL80
```

**Linux/Mac:**
```bash
sudo systemctl start mysql
```

### 1b. Create the Database & Tables

Run the schema script to create the database, tables, triggers, stored procedures, and seed data:

```bash
# Command line
mysql -u root -p < src/main/resources/db/schema.sql
```

Or in MySQL Workbench / phpMyAdmin:
```sql
SOURCE path/to/project/src/main/resources/db/schema.sql;
```

### 1c. Default Login Credentials

| Field | Value |
|-------|-------|
| **Username** | `admin` |
| **Password** | `admin123` |

---

## Step 2 — Configure Database Connection

Open `WEB-INF/application.properties` and update if needed:

```properties
db.url=jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=          # Add your MySQL root password here if set
```

> By default, XAMPP MySQL has **no password** for `root` — leave `db.password` blank.

---

## Step 3 — Build the Project

From the project root directory, run:

```bash
mvn clean package -DskipTests
```

A successful build outputs:
```
[INFO] Building war: ...\target\project.war
[INFO] BUILD SUCCESS
```

---

## Step 4 — Deploy & Run on Tomcat

### Option A — XAMPP Tomcat (Recommended)

The project folder is already inside `webapps/`. Start Tomcat from PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:CATALINA_HOME = "C:\xampp\tomcat"
C:\xampp\tomcat\bin\catalina.bat run
```

Or use the **XAMPP Control Panel** → click **Start** next to **Tomcat**.

### Option B — Standalone Tomcat

1. Copy the WAR file:
   ```bash
   copy target\project.war C:\path\to\tomcat\webapps\
   ```
2. Start Tomcat:
   ```bash
   # Windows
   C:\path\to\tomcat\bin\startup.bat

   # Linux/Mac
   ./path/to/tomcat/bin/startup.sh
   ```

### Verify Tomcat is Running

```powershell
netstat -ano | findstr ":8080"
# Should show LISTENING on port 8080
```

---

## Step 5 — Access the Application

Open your browser and navigate to:

| Page | URL |
|------|-----|
| 🔐 **Login** | http://localhost:8080/project/ |
| 📊 **Dashboard** | http://localhost:8080/project/dashboard.html |
| 📅 **Appointments** | http://localhost:8080/project/appointments.html |
| 💰 **Billing** | http://localhost:8080/project/billing.html |
| 🔍 **Search** | http://localhost:8080/project/search.html |
| 📈 **Reports** | http://localhost:8080/project/reports.html |
| ❓ **Help** | http://localhost:8080/project/help.html |

---

## 🧪 Running Tests

The project uses **JUnit 5** with an **H2 in-memory database** — no live MySQL connection required.

```bash
# Run all tests
mvn test
```

Expected output:
```
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Run a specific test class:
```bash
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=AppointmentServiceTest
mvn test -Dtest=BillingServiceTest
```

---

## 🛑 Stopping the Server

```powershell
# Windows
C:\xampp\tomcat\bin\catalina.bat stop

# Or press Ctrl+C in the terminal where Tomcat is running
```

---

## 🔧 Troubleshooting

### Port 8080 already in use
```powershell
netstat -ano | findstr ":8080"
taskkill /PID <PID> /F    # Replace <PID> with the actual process ID
```

### Database connection refused
- Ensure MySQL is running on port `3306`
- Verify `db.user` and `db.password` in `WEB-INF/application.properties`
- Make sure `sunrise_dental_db` exists — re-run `schema.sql`

### ClassNotFoundException or servlet errors
- Rebuild with Maven: `mvn clean package -DskipTests`
- Confirm all JARs are present in `WEB-INF/lib/`

### Maven build fails
- Check Java version: `java -version` (must be 21+)
- Set JAVA_HOME:
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
  ```

### Login not working
- Re-run `schema.sql` to seed the default admin user
- Credentials: `admin` / `admin123`

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | HTML5, CSS3, Vanilla JavaScript |
| **Backend** | Java 21, Java Servlets (javax.servlet 3.1) |
| **Database** | MySQL 8.0 with HikariCP connection pool |
| **Build Tool** | Apache Maven 3.9 |
| **Server** | Apache Tomcat 8.5 |
| **Testing** | JUnit 5, Mockito, H2 In-Memory DB |
| **CI/CD** | GitHub Actions |

---

## 📄 Related Documents

- [`TEST_PLAN_AND_TDD.md`](TEST_PLAN_AND_TDD.md) — Full test plan and TDD documentation
- [`REPORT_CIS6003.md`](REPORT_CIS6003.md) — Project report
- [`UML_DIAGRAMS.md`](UML_DIAGRAMS.md) — System architecture and UML diagrams
- [`GIT_WORKFLOW_AND_CICD.md`](GIT_WORKFLOW_AND_CICD.md) — Git branching strategy and CI/CD pipeline

---

*Cardiff Metropolitan University — CIS6003 | Sunrise Dental Clinic Management System*
