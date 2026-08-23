# Task C: Test Plan & Test-Driven Development (CIS6003 - LO II)

**Module:** CIS6003 Advanced Programming  
**Institution:** Cardiff Metropolitan University / School of Technologies  
**Project:** Sunrise Dental Clinic Management System  
**Test Framework:** JUnit 5 (Jupiter), Mockito 5.11, Maven Surefire Automated Engine  

---

## 1. Test-Driven Development (TDD) Rationale & Methodology

The Sunrise Dental Clinic Management System handles sensitive clinical appointments, doctor calendars, and financial transactions. A defect in appointment validation could cause conflicting double-bookings, while an error in fee calculations could cause financial losses or billing disputes.

To guarantee high software craftsmanship, reliability, and correctness, **Test-Driven Development (TDD)** was adopted using the rigorous **Red-Green-Refactor Cycle**:

```
 ┌─────────────────────────────────────────────────────────────┐
 │ 1. RED PHASE                                                │
 │ Write automated unit tests for desired functionality before │
 │ implementing production code. Verify that tests fail.      │
 └──────────────────────────────┬──────────────────────────────┘
                                │
 ┌──────────────────────────────▼──────────────────────────────┐
 │ 2. GREEN PHASE                                              │
 │ Write minimal, clean production code to pass all test cases │
 │ successfully.                                               │
 └──────────────────────────────┬──────────────────────────────┘
                                │
 ┌──────────────────────────────▼──────────────────────────────┐
 │ 3. REFACTOR PHASE                                           │
 │ Optimize code structure, eliminate redundancy, apply design │
 │ patterns (Strategy, DAO, Singleton), while retaining tests. │
 └─────────────────────────────────────────────────────────────┘
```

---

## 2. Comprehensive Test Plan & Test Case Data

The test suite covers positive functional flows, negative boundary conditions, schedule conflict checks, and input format validation.

| Test Case ID | Target Module / Class | Test Objective | Test Data / Inputs | Expected Output | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-AUTH-01** | `AuthService` | Successful login with valid credentials | User: `"reception"`, Pwd: `"password123"` | Returns `User` object (`RECEPTIONIST`) | **PASS** |
| **TC-AUTH-02** | `AuthService` | Rejection of incorrect password | User: `"admin"`, Pwd: `"wrongPwd"` | Returns `Optional.empty()` | **PASS** |
| **TC-AUTH-03** | `AuthService` | Rejection of non-existent user | User: `"unknown"`, Pwd: `"any"` | Returns `Optional.empty()` | **PASS** |
| **TC-AUTH-04** | `AuthService` | Blank / whitespace username handling | User: `""`, Pwd: `"pwd"` | Rejection without DB query | **PASS** |
| **TC-AUTH-05** | `AuthService` | Inactive account login prevention | Inactive account, valid pwd | Rejection (`is_active = false`) | **PASS** |
| **TC-AUTH-06** | `AuthService` | SHA-256 hash determinism | Input: `"admin123"` | Consistent 64-char hex hash | **PASS** |
| **TC-BILL-01** | `StandardBillingStrategy` | Standard bill calculation without discount | Consult: 2,500, Treatment: 4,500 | Subtotal: 7,000.00, Total: 7,000.00 | **PASS** |
| **TC-BILL-02** | `StandardBillingStrategy` | Bill with 10% discount and 5% tax | Consult: 3,000, Treatment: 18,000, 10% disc, 5% tax | Subtotal: 21,000.00, Total: 19,845.00 | **PASS** |
| **TC-BILL-03** | `SeniorDiscountBillingStrategy` | Senior Care Strategy (10% base discount) | Consult: 2,500, Treatment: 7,500, 5% promo disc | Total Discount: 15% (1,500.00), Total: 8,500.00 | **PASS** |
| **TC-BILL-04** | `SurgicalBillingStrategy` | Surgical Strategy with Sterilization Surcharge | Consult: 3,500, Treatment: 12,500 (+1,500 surcharge) | Subtotal: 17,500.00, Total: 17,500.00 | **PASS** |
| **TC-BILL-05** | `StandardBillingStrategy` | 100% Full Discount Boundary Condition | Consult: 3,000, Treatment: 5,000, 100% disc | Subtotal: 8,000.00, Total: 0.00 | **PASS** |
| **TC-BILL-06** | `BillingService` | End-to-end invoice generation flow | Appointment ID: 10, Cash settlement | Bill record persisted with unique invoice # | **PASS** |
| **TC-APP-01** | `AppointmentService` | Reject appointment with date in the past | Date: `today - 1 day`, Time: 10:00 AM | Validation error ("cannot be in past") | **PASS** |
| **TC-APP-02** | `AppointmentService` | Reject doctor double-booking conflict | Dentist: 2, Date: 2026-08-25, Time: 09:30 AM (occupied) | Validation error ("already booked") | **PASS** |
| **TC-APP-03** | `AppointmentService` | Accept valid appointment schedule | Dentist: 1, Future Date, Available Time | Passes schedule validation (`true`) | **PASS** |
| **TC-APP-04** | `AppointmentService` | Complete appointment booking workflow | Valid patient, doctor, treatment | Generates `APP-2026-099` + notification | **PASS** |
| **TC-APP-05** | `AppointmentService` | Search appointment by unique ID | Search query: `"APP-2026-001"` | Returns matching Appointment entity | **PASS** |
| **TC-PAT-01** | `PatientService` | Valid patient registration validation | Name: "Sanduni", Phone: "0771234567" | Validation succeeds (`true`) | **PASS** |
| **TC-PAT-02** | `PatientService` | Valid phone numbers (+94, 077, 071, 011) | `"0771234567"`, `"+94771234567"` | All formats pass regex validation | **PASS** |
| **TC-PAT-03** | `PatientService` | Invalid phone numbers rejected | `"12345"`, `"phone"`, `"077-1234"` | Rejected by format validator | **PASS** |
| **TC-PAT-04** | `PatientService` | Patient name too short (< 2 characters) | Name: `"A"` | Rejection ("at least 2 characters") | **PASS** |
| **TC-PAT-05** | `PatientService` | Missing residential address rejection | Address: `""` | Rejection ("address is required") | **PASS** |

---

## 3. Automated Test Execution Evidence (Maven Surefire Output)

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.sunrisedental.AppointmentValidationTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.756 s
Running com.sunrisedental.AuthServiceTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.406 s
Running com.sunrisedental.BillingServiceTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.195 s
Running com.sunrisedental.PatientValidationTest
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.109 s

Results:
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 4. Requirements Traceability Matrix

| Requirement | Brief Reference | Test Case IDs | Verification Mechanism |
| :--- | :--- | :--- | :--- |
| **1. User Authentication (Login)** | Page 2, Req 1 | `TC-AUTH-01` to `TC-AUTH-06` | JUnit 5 unit tests for SHA-256 authentication & session guard |
| **2. Register New Appointment** | Page 2, Req 2 | `TC-APP-01` to `TC-APP-04`, `TC-PAT-01` to `TC-PAT-05` | Conflict detection, date validation, phone format tests |
| **3. Display Appointment Details** | Page 2, Req 3 | `TC-APP-05` | Search by appointment number & relational joins |
| **4. Calculate and Print Bill** | Page 2, Req 4 | `TC-BILL-01` to `TC-BILL-06` | Strategy pattern tests, discount bounds, stored procedure invoice |
| **5. Double-Booking Prevention** | Page 3, Req (ii/iii) | `TC-APP-02` | MySQL database trigger `trg_prevent_double_booking` & Java validation |
| **6. Continuous Integration** | Page 4, Task D | GitHub Actions CI/CD | Automated test run on every push & pull request |
