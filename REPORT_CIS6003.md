# ADVANCED PROGRAMMING (CIS6003)
## COURSEWORK ASSESSMENT REPORT (WRIT1 - 100%)
### Enterprise Distributed Patient & Appointment Management System with CI/CD Pipeline
**Case Study:** Sunrise Dental Clinic, Colombo  

---

**Student / Candidate Information:**  
- **Module Code:** CIS6003  
- **Module Title:** Advanced Programming  
- **Academic Year / Semester:** 2024 / Semester 1  
- **Awarding Institution:** Cardiff Metropolitan University / ICBT Campus  
- **Target Grade Band:** Excellent (70 - 100%)  
- **Submission Title:** Sunrise Dental Clinic Computerized Management System  

---

## TABLE OF CONTENTS
1. [Executive Summary](#1-executive-summary)
2. [Section 1: Problem Scenario & Requirements Analysis](#2-section-1-problem-scenario--requirements-analysis)
3. [Section 2: Task A – System Design with UML Diagrams (LO I)](#3-section-2-task-a--system-design-with-uml-diagrams-lo-i)
   - 2.1 Design Decisions & Architectural Assumptions
   - 2.2 Use Case Modeling with Include & Extend Stereotypes
   - 2.3 Comprehensive Class Diagram
   - 2.4 Detailed Sequence Diagrams
   - 2.5 Critical Reflection & Design Evaluation
4. [Section 3: Task B – Distributed Architecture, Design Patterns & Database Development (LO II)](#4-section-3-task-b--distributed-architecture-design-patterns--database-development-lo-ii)
   - 3.1 Distributed 3-Tier Architecture & RESTful Web Services
   - 3.2 Implementation & Evaluation of Design Patterns
   - 3.3 Advanced Database Architecture (Triggers, Stored Procedures, Functions)
   - 3.4 User Interface, Session Management & Decision-Making Reports
5. [Section 4: Task C – Test-Driven Development & Test Automation (LO II)](#5-section-4-task-c--test-driven-development--test-automation-lo-ii)
   - 4.1 Test-Driven Development (TDD) Approach & Rationale
   - 4.2 Comprehensive Test Plan & Test Case Data
   - 4.3 Automated Test Execution & Pass Validation
   - 4.4 Requirements Traceability Matrix
6. [Section 5: Task D – Git Version Control & GitHub Actions CI/CD Pipeline (LO III)](#6-section-5-task-d--git-version-control--github-actions-cicd-pipeline-lo-iii)
   - 5.1 Version Control Architecture & Git Flow Strategy
   - 5.2 Commit Progression & Version History
   - 5.3 Automated CI/CD Workflow (Build, Test, Package, Deploy)
7. [Section 6: Cardiff Met EDGE Integration](#7-section-6-cardiff-met-edge-integration)
8. [Section 7: Conclusion & Future Enhancements](#8-section-7-conclusion--future-enhancements)
9. [Section 8: Harvard Reference List](#9-section-8-harvard-reference-list)

---

## 1. Executive Summary

In contemporary healthcare administration, reliance on physical record-keeping and manual scheduling leads to severe operational bottlenecks, patient appointment double-bookings, billing calculation inaccuracies, and administrative inefficiencies (Sommerville, 2020). This project presents the design, architectural engineering, implementation, automated testing, and CI/CD deployment of the **Sunrise Dental Clinic Management System**, an enterprise-grade distributed software solution tailored for a private dental clinic in Colombo, Sri Lanka.

The solution is developed using Java 23, Apache Maven, MySQL/MariaDB, and modern Web standards. It adheres to a **Distributed Three-Tier Architecture** exposing RESTful Web Services consumed by a responsive Single-Page Presentation Tier. Enterprise **Design Patterns**—including *Singleton*, *Data Access Object (DAO)*, *Factory*, *Strategy*, *Observer*, and *Model-View-Controller (MVC)*—are rigorously implemented to guarantee high cohesion, low coupling, and open-ended extensibility (Gamma et al., 1994). Database integrity is enforced through triggers preventing double-booking conflicts, stored procedures executing atomic invoicing, and deterministic stored functions calculating periodic revenues. The system was engineered following **Test-Driven Development (TDD)** using JUnit 5, validated with a 100% automated test pass rate, and deployed via a multi-stage **GitHub Actions CI/CD pipeline**.

---

## 2. Section 1: Problem Scenario & Requirements Analysis

### 1.1 Scenario Overview
Sunrise Dental Clinic is a premier healthcare facility providing routine consultations, restorative procedures, surgical operations, and orthodontic care in Colombo. Previously, clinic appointments and financial billing were recorded manually using paper logbooks. This manual workflow suffered from:
- **Scheduling Conflicts & Double-Bookings:** Multiple receptionists booking the same doctor in overlapping time slots without real-time synchronization.
- **Misplaced Patient Histories:** Lost contact information, medical allergies, and previous diagnostic notes.
- **Billing Errors:** Inaccurate calculations of consultation fees, variable treatment costs, discounts, and healthcare taxes.
- **Lack of Decision-Making Analytics:** Absence of management reporting regarding doctor clinical workloads and daily cash flows.

### 1.2 Functional & Non-Functional Requirements
- **FR1 (User Authentication):** Secure credential verification with SHA-256 password hashing and role-based HTTP session management (Admin, Receptionist, Dentist).
- **FR2 (Register New Appointment):** Interactive capture of patient demographics, dentist selection, treatment cataloging, appointment scheduling, and automated unique ID assignment (`APP-YYYY-NNN`).
- **FR3 (Display Appointment Details):** Instant retrieval and relational visualization of patient, doctor, procedure, and payment status by unique appointment number.
- **FR4 (Calculate and Print Bill):** Automated computation of consultation fees, procedure costs, custom discounts, and tax rates via Strategy patterns, accompanied by printable itemized receipts.
- **FR5 (Management Reports & Decision Support):** Real-time doctor workload tracking and date-range financial summaries.
- **FR6 (Help & Safe Exit):** Step-by-step interactive staff documentation and secure session invalidation.
- **NFR1 (Concurrency & Data Integrity):** Zero-tolerance for overlapping doctor slots enforced at database and service tiers.
- **NFR2 (Security & Compliance):** Protection of Personally Identifiable Information (PII) and password non-reversibility.

---

## 3. Section 2: Task A – System Design with UML Diagrams (LO I)

### 2.1 Design Decisions & Architectural Assumptions
1. **Stateless Service Layer with Session Cookies:** Business logic is decoupled into stateless services (`AppointmentService`, `BillingService`, `AuthService`) while client authentication state is secured using HTTP Session cookies (`JSESSIONID`).
2. **Deterministic Doctor Slot Allocation:** Clinic consultations operate in fixed 30-minute blocks between 09:00 and 17:00.
3. **Relational Database Normalization:** Applied Third Normal Form (3NF) across entities (`users`, `patients`, `dentists`, `treatments`, `appointments`, `bills`, `audit_logs`).

### 2.2 Use Case Modeling
The Use Case Diagram establishes boundaries, actors, and relationships (`<<include>>` and `<<extend>>`):
- Booking an appointment **includes** schedule validation and **extends** automated SMS/Email notification dispatch.
- Generating a bill **includes** fee calculation and receipt formatting, while **extending** senior citizen subsidies and alert dispatches.

*(Refer to `UML_DIAGRAMS.md` for full Mermaid models).*

### 2.3 Comprehensive Class Diagram
The class hierarchy defines strong typing, visibility modifiers (`-`, `+`), parameters, return types, and multiplicities:
- `DatabaseConnection` enforces a thread-safe Singleton connection pool via `HikariDataSource`.
- `IBillingStrategy` interface abstracts fee calculation algorithms implemented by `StandardBillingStrategy`, `SeniorDiscountBillingStrategy`, and `SurgicalBillingStrategy`.
- `DAOFactory` decouples DAO creation from client consumers.
- Relationships depict Composition (Appointment to Bill), Aggregation (Service to DAO), and Generalization (Strategy implementations).

### 2.4 Detailed Sequence Diagrams
Three core operational scenarios are documented:
1. **User Authentication & Session Login:** Verifies SHA-256 hash against database record and establishes `HttpSession`.
2. **Register Appointment with Conflict Checking:** Validates date/time constraints, executes conflict query, persists appointment, and invokes `NotificationService`.
3. **Fee Calculation & Invoice Settlement:** Executes Strategy pattern fee calculations, triggers MySQL stored procedure `sp_GeneratePatientInvoice`, and renders the printable receipt.

### 2.5 Critical Reflection on System Design
The design guarantees **Separation of Concerns (SoC)**. Encapsulating persistence behind DAO interfaces ensures that database optimizations or migrations do not propagate errors into the UI or business rules (Fowler, 2002).

---

## 4. Section 3: Task B – Distributed Architecture, Design Patterns & Database Development (LO II)

### 3.1 Distributed 3-Tier Architecture & RESTful Web Services
The system is partitioned into three distinct tiers communicating via standardized protocols:

```
[Presentation Tier: Browser / Modern Web UI]
         ▲
         │ JSON over HTTP (REST API Endpoints: /api/appointments, /api/bills)
         ▼
[Business Logic Tier: Java Servlets / Services / Design Patterns]
         ▲
         │ JDBC Connection Pool (HikariCP)
         ▼
[Data Access Tier: MySQL Database / Stored Procedures / Triggers]
```

REST endpoints serialize domain models into clean JSON payloads via Google Gson, providing a lightweight, distributed communication channel.

### 3.2 Implementation & Evaluation of Design Patterns

| Design Pattern | Concrete Class | Architectural Justification & Evaluation |
| :--- | :--- | :--- |
| **Singleton** | `DatabaseConnection.java` | Controls resource allocation by ensuring exactly one thread-safe HikariCP connection pool exists across all concurrent requests, preventing socket exhaustion. |
| **Data Access Object (DAO)** | `AppointmentDAOImpl.java`, `BillingDAOImpl.java`, `UserDAOImpl.java` | Isolates SQL syntax and relational mappings from business rules, promoting testability with in-memory mocks. |
| **Factory Method** | `DAOFactory.java`, `BillingCalculatorFactory.java` | Centralizes object creation and instantiates appropriate billing strategies based on patient age or treatment category. |
| **Strategy** | `IBillingStrategy.java`, `StandardBillingStrategy`, `SeniorDiscountBillingStrategy` | Eliminates complex `if-else` branching by encapsulating fee and discount algorithms in interchangeable strategy classes. |
| **Observer (Publish-Subscribe)** | `NotificationService.java` | Decouples appointment/billing events from external alert communication mechanisms (simulated SMS & Email gateways). |
| **MVC / Front Controller** | `BaseServlet.java`, Web Views | Separates presentation markup from request handling and domain logic. |

### 3.3 Advanced Database Architecture

```sql
-- Trigger: Double Booking Prevention
CREATE TRIGGER trg_prevent_double_booking
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE conflict_count INT;
    SELECT COUNT(*) INTO conflict_count FROM appointments
    WHERE dentist_id = NEW.dentist_id AND appointment_date = NEW.appointment_date 
      AND appointment_time = NEW.appointment_time AND status != 'CANCELLED';
    IF conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Dentist scheduling conflict.';
    END IF;
END;
```

- **Stored Procedure (`sp_GeneratePatientInvoice`):** Atomically calculates fees, computes tax and discount amounts, persists the bill record, and updates appointment status to `COMPLETED`.
- **Stored Function (`fn_CalculateTotalRevenue`):** Deterministic aggregation computing total clinic revenue between customizable dates.
- **Stored Procedure (`sp_GetDoctorWorkloadReport`):** Aggregates total, completed, and pending appointments alongside generated revenue per dentist.

### 3.4 Decision-Making Reports & UI Features
- **Interactive Dashboard:** Live stat cards displaying registered patients, scheduled appointments, and daily revenue.
- **Printable Official Receipt:** A dedicated, printer-friendly CSS stylesheet generating formatted patient receipts with clinic branding, doctor details, and payment breakdown.
- **Staff User Manual (`help.html`):** Integrated step-by-step guidance assisting newly onboarded receptionists.

---

## 5. Section 4: Task C – Test-Driven Development & Test Automation (LO II)

### 5.1 TDD Methodology & Rationale
Test-Driven Development was practiced throughout the application's construction. For every critical business rule (such as past-date rejection, duplicate slot prevention, and senior citizen discount computation), JUnit 5 test classes were drafted and executed to fail (Red), followed by implementation of production code (Green), and subsequent refactoring for performance and pattern adherence (Refactor) (Beck, 2003).

### 5.2 Automated Test Execution
Automated testing is integrated into the Maven lifecycle via `maven-surefire-plugin`. 30 automated test cases were executed across 4 test suites:
- `AuthServiceTest` (8 tests): Password hashing, valid/invalid login, inactive accounts.
- `BillingServiceTest` (6 tests): Strategy calculations, senior citizen discounts, tax rules, 100% discount boundary.
- `AppointmentValidationTest` (5 tests): Conflict rejection, past date blocking, booking workflows.
- `PatientValidationTest` (11 tests): Name lengths, mandatory fields, Sri Lankan phone formats (`0771234567`, `+94...`).

**Test Execution Outcome:**
```
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 14.88 s
[INFO] BUILD SUCCESS
```

---

## 6. Section 5: Task D – Git Version Control & GitHub Actions CI/CD Pipeline (LO III)

### 6.1 Git Flow Versioning Strategy
The project repository was established with Git Flow standards, utilizing semantic tags (`v0.1.0` through `v1.0.0`) and structured, atomic commits:
1. `cad18e5` - `feat(init)`: Maven configuration and database schema.
2. `f01172b` - `feat(dao)`: Domain models and DAO persistence.
3. `6bbfc76` - `feat(service)`: Strategy pattern billing and observer alerts.
4. `887edc1` - `feat(api)`: REST servlets and session filters.
5. `d2356e1` - `feat(ui)`: Responsive UI, printable invoices, and help manual.
6. `b2fdee2` - `test(tdd)`: JUnit 5 test automation suite.
7. `81cec3f` - `ci(pipeline)`: Multi-stage GitHub Actions CI/CD workflow.

### 6.2 GitHub Actions CI/CD Pipeline
The workflow (`.github/workflows/ci-cd.yml`) automates four essential stages:
- **Build Stage:** Configures JDK 23 and verifies compile-time syntax.
- **Test Stage:** Automatically executes all 30 JUnit test cases and archives Surefire HTML reports.
- **Package Stage:** Compiles the deployable production archive `project.war`.
- **Continuous Deployment Stage:** Deploys the artifact to the Apache Tomcat servlet container and performs health verification.

---

## 7. Section 6: Cardiff Met EDGE Integration

The development of the Sunrise Dental Clinic system incorporates the core pillars of the Cardiff Met EDGE:
- **Ethical:** User passwords are encrypted using irreversible SHA-256 hashing. Sensitive patient health data is isolated with session authentication, ensuring compliance with international data privacy standards (GDPR/Data Protection Act).
- **Digital:** Employs advanced distributed web services, asynchronous AJAX communications, automated CI/CD pipelines, and cloud-ready container packaging.
- **Global:** Currency formatting, standardized telephone validation regex (`+94`), and responsive international layout designs accommodate global clinical standards.
- **Entrepreneurial:** Provides clinic leadership with data-driven decision reports, revenue optimization analytics, and doctor workload tracking to streamline business expansion.

---

## 8. Section 7: Conclusion & Future Enhancements

The Sunrise Dental Clinic Management System fulfills all technical, design, architectural, testing, and CI/CD criteria outlined in the CIS6003 Advanced Programming coursework brief. By combining a 3-tier distributed architecture, design patterns, automated TDD test suites, and GitHub Actions CI/CD workflows, the system achieves maximum reliability, scalability, and maintainability.

**Future Recommendations:**
1. Integration with cloud payment gateways (Stripe / Commercial Bank IPG).
2. Direct integration with SMS gateways (Twilio / Dialog Axiata SMS API).
3. Implementation of OAuth2 / JWT authentication for third-party mobile applications.

---

## 9. Section 8: Harvard Reference List

- Beck, K., 2003. *Test-Driven Development: By Example*. Boston: Addison-Wesley Professional.
- Fowler, M., 2002. *Patterns of Enterprise Application Architecture*. Boston: Addison-Wesley Longman Publishing Co., Inc.
- Freeman, E., Robson, E., Bates, B. and Sierra, K., 2004. *Head First Design Patterns*. Sebastopol: O'Reilly Media.
- Gamma, E., Helm, R., Johnson, R. and Vlissides, J., 1994. *Design Patterns: Elements of Reusable Object-Oriented Software*. Reading: Addison-Wesley.
- Martin, R.C., 2008. *Clean Code: A Handbook of Agile Software Craftsmanship*. Upper Saddle River: Prentice Hall.
- Oracle Corporation, 2024. *Java Platform, Standard Edition Documentation (JDK 23)*. Redwood City: Oracle. Available at: <https://docs.oracle.com/en/java/javase/23/> [Accessed 23 August 2026].
- Pressman, R.S. and Maxim, B.R., 2020. *Software Engineering: A Practitioner's Approach*. 9th ed. New York: McGraw-Hill Education.
- Sommerville, I., 2020. *Software Engineering*. 10th ed. Harlow: Pearson Education Limited.
