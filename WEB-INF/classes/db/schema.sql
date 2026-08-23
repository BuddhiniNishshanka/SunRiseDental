-- ==========================================================
-- Sunrise Dental Clinic Management System Database Schema
-- Cardiff Metropolitan University - CIS6003 Advanced Programming
-- ==========================================================

DROP DATABASE IF EXISTS sunrise_dental_db;
CREATE DATABASE sunrise_dental_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sunrise_dental_db;

-- 1. Users Table (System Authentication & Authorization)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'RECEPTIONIST', 'DENTIST') NOT NULL DEFAULT 'RECEPTIONIST',
    email VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Patients Table
CREATE TABLE patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    contact_no VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    dob DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Dentists Table
CREATE TABLE dentists (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    room_no VARCHAR(20) NOT NULL,
    contact_no VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    consultation_fee DECIMAL(10, 2) NOT NULL DEFAULT 2500.00,
    is_available BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- 4. Treatments Table
CREATE TABLE treatments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    treatment_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    standard_fee DECIMAL(10, 2) NOT NULL,
    category ENUM('GENERAL', 'SURGICAL', 'COSMETIC', 'ORTHODONTIC') NOT NULL DEFAULT 'GENERAL'
) ENGINE=InnoDB;

-- 5. Appointments Table
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_no VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    dentist_id INT NOT NULL,
    treatment_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_app_dentist FOREIGN KEY (dentist_id) REFERENCES dentists(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_app_treatment FOREIGN KEY (treatment_id) REFERENCES treatments(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_app_date (appointment_date),
    INDEX idx_app_patient (patient_id),
    INDEX idx_app_dentist (dentist_id)
) ENGINE=InnoDB;

-- 6. Billing / Invoices Table
CREATE TABLE bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_no VARCHAR(30) NOT NULL UNIQUE,
    appointment_id INT NOT NULL UNIQUE,
    consultation_fee DECIMAL(10, 2) NOT NULL,
    treatment_cost DECIMAL(10, 2) NOT NULL,
    discount_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_status ENUM('PENDING', 'PAID', 'REFUNDED') NOT NULL DEFAULT 'PAID',
    payment_method ENUM('CASH', 'CREDIT_CARD', 'ONLINE_TRANSFER', 'INSURANCE') NOT NULL DEFAULT 'CASH',
    bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 7. Audit Log Table (Enterprise Security & Compliance)
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45) DEFAULT '127.0.0.1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==========================================================
-- ADVANCED DATABASE FEATURES (Triggers, Stored Procedures, Functions)
-- ==========================================================

-- Trigger: Prevent Double Booking for the same Dentist at identical date/time
DELIMITER $$
CREATE TRIGGER trg_prevent_double_booking
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE conflict_count INT;
    
    SELECT COUNT(*) INTO conflict_count
    FROM appointments
    WHERE dentist_id = NEW.dentist_id
      AND appointment_date = NEW.appointment_date
      AND appointment_time = NEW.appointment_time
      AND status != 'CANCELLED';
      
    IF conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Dentist scheduling conflict: Dentist already has an active appointment at this date and time.';
    END IF;
END$$

-- Stored Procedure: Generate Patient Invoice with Transactional Safety
CREATE PROCEDURE sp_GeneratePatientInvoice(
    IN p_appointment_id INT,
    IN p_discount_rate DECIMAL(5,2),
    IN p_tax_rate DECIMAL(5,2),
    IN p_payment_method VARCHAR(20),
    OUT p_bill_id INT,
    OUT p_total_amount DECIMAL(10,2)
)
BEGIN
    DECLARE v_consultation_fee DECIMAL(10,2);
    DECLARE v_treatment_cost DECIMAL(10,2);
    DECLARE v_subtotal DECIMAL(10,2);
    DECLARE v_discount_amount DECIMAL(10,2);
    DECLARE v_tax_amount DECIMAL(10,2);
    DECLARE v_final_total DECIMAL(10,2);
    DECLARE v_bill_no VARCHAR(30);
    
    -- Extract fees from dentist and treatment linked to appointment
    SELECT d.consultation_fee, t.standard_fee
    INTO v_consultation_fee, v_treatment_cost
    FROM appointments a
    JOIN dentists d ON a.dentist_id = d.id
    JOIN treatments t ON a.treatment_id = t.id
    WHERE a.id = p_appointment_id;
    
    -- Computations
    SET v_subtotal = v_consultation_fee + v_treatment_cost;
    SET v_discount_amount = ROUND((v_subtotal * p_discount_rate / 100.0), 2);
    SET v_tax_amount = ROUND(((v_subtotal - v_discount_amount) * p_tax_rate / 100.0), 2);
    SET v_final_total = ROUND((v_subtotal - v_discount_amount + v_tax_amount), 2);
    
    -- Unique Bill Number
    SET v_bill_no = CONCAT('INV-', YEAR(CURDATE()), '-', LPAD(p_appointment_id, 4, '0'));
    
    -- Insert / Upsert into bills table
    INSERT INTO bills (
        bill_no, appointment_id, consultation_fee, treatment_cost,
        discount_rate, discount_amount, tax_rate, tax_amount,
        total_amount, payment_status, payment_method, bill_date
    ) VALUES (
        v_bill_no, p_appointment_id, v_consultation_fee, v_treatment_cost,
        p_discount_rate, v_discount_amount, p_tax_rate, v_tax_amount,
        v_final_total, 'PAID', p_payment_method, NOW()
    )
    ON DUPLICATE KEY UPDATE
        consultation_fee = v_consultation_fee,
        treatment_cost = v_treatment_cost,
        discount_rate = p_discount_rate,
        discount_amount = v_discount_amount,
        tax_rate = p_tax_rate,
        tax_amount = v_tax_amount,
        total_amount = v_final_total,
        payment_method = p_payment_method,
        bill_date = NOW();
        
    -- Update appointment status to COMPLETED
    UPDATE appointments SET status = 'COMPLETED' WHERE id = p_appointment_id;
    
    SELECT id, total_amount INTO p_bill_id, p_total_amount FROM bills WHERE appointment_id = p_appointment_id;
END$$

-- Stored Function: Calculate Total Revenue over given date range
CREATE FUNCTION fn_CalculateTotalRevenue(
    p_start_date DATE,
    p_end_date DATE
)
RETURNS DECIMAL(12,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(12,2);
    
    SELECT IFNULL(SUM(total_amount), 0.00) INTO v_total
    FROM bills
    WHERE DATE(bill_date) BETWEEN p_start_date AND p_end_date
      AND payment_status = 'PAID';
      
    RETURN v_total;
END$$

-- Stored Procedure: Get Doctor Workload Report
CREATE PROCEDURE sp_GetDoctorWorkloadReport()
BEGIN
    SELECT 
        d.id AS dentist_id,
        d.name AS dentist_name,
        d.specialization,
        COUNT(a.id) AS total_appointments,
        SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_appointments,
        SUM(CASE WHEN a.status = 'SCHEDULED' THEN 1 ELSE 0 END) AS pending_appointments,
        IFNULL(SUM(b.total_amount), 0.00) AS total_revenue_generated
    FROM dentists d
    LEFT JOIN appointments a ON d.id = a.dentist_id
    LEFT JOIN bills b ON a.id = b.appointment_id
    GROUP BY d.id, d.name, d.specialization
    ORDER BY total_appointments DESC;
END$$
DELIMITER ;

-- ==========================================================
-- SEED DATA (Realistic Setup for Sunrise Dental Clinic)
-- ==========================================================

-- Seed Users (Passwords: 'admin123' and 'staff123' hashed with SHA-256)
-- admin123 -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- staff123 -> 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918
INSERT INTO users (username, password_hash, full_name, role, email) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Dr. Priyanga Jayawardena (Clinic Director)', 'ADMIN', 'admin@sunrisedental.lk'),
('reception', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Kavindi Perera (Front Desk)', 'RECEPTIONIST', 'kavindi@sunrisedental.lk'),
('dentist_silva', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Dr. Nuwan Silva', 'DENTIST', 'nsilva@sunrisedental.lk');

-- Seed Dentists
INSERT INTO dentists (name, specialization, room_no, contact_no, email, consultation_fee) VALUES
('Dr. Nuwan Silva', 'Orthodontics & Dental Surgery', 'Room 101', '0771234567', 'nsilva@sunrisedental.lk', 3000.00),
('Dr. Anoma Wijesinghe', 'Pediatric & General Dentistry', 'Room 102', '0772345678', 'awijesinghe@sunrisedental.lk', 2500.00),
('Dr. Ramesh Fernando', 'Periodontics & Implantology', 'Room 103', '0773456789', 'rfernando@sunrisedental.lk', 3500.00),
('Dr. Dilani Rajapaksha', 'Cosmetic Dentistry & Endodontics', 'Room 104', '0774567890', 'drajapaksha@sunrisedental.lk', 2800.00);

-- Seed Treatments
INSERT INTO treatments (treatment_code, name, description, standard_fee, category) VALUES
('TRT-001', 'Standard Dental Checkup & Cleaning', 'Comprehensive oral examination, plaque removal and scaling', 3500.00, 'GENERAL'),
('TRT-002', 'Composite Tooth Filling', 'Tooth-colored composite resin filling for cavity restoration', 4500.00, 'GENERAL'),
('TRT-003', 'Root Canal Therapy (Single Canal)', 'Complete pulpectomy and root canal disinfection', 18000.00, 'SURGICAL'),
('TRT-004', 'Surgical Tooth Extraction', 'Wisdom tooth or complex extraction under local anesthesia', 12500.00, 'SURGICAL'),
('TRT-005', 'Laser Teeth Whitening', 'Full arch laser cosmetic teeth whitening session', 25000.00, 'COSMETIC'),
('TRT-006', 'Orthodontic Braces Consultation', 'Initial orthodontic assessment, dental impressions and treatment plan', 8000.00, 'ORTHODONTIC'),
('TRT-007', 'Dental Crown / Bridge Fitting', 'Porcelain ceramic crown fitting and adjustment', 22000.00, 'COSMETIC');

-- Seed Patients
INSERT INTO patients (patient_code, full_name, address, contact_no, email, dob, gender) VALUES
('PT-1001', 'Kamal Gunaratne', 'No. 45/2, Galle Road, Colombo 03', '0714567890', 'kamal.g@gmail.com', '1988-05-14', 'MALE'),
('PT-1002', 'Sanduni Fernando', '128 Temple Road, Mount Lavinia', '0778901234', 'sanduni.f@outlook.com', '1995-11-20', 'FEMALE'),
('PT-1003', 'Mohamed Rizwan', '74 Ward Place, Colombo 07', '0751239876', 'm.rizwan@yahoo.com', '1982-03-08', 'MALE'),
('PT-1004', 'Nimali De Silva', '15 Kandy Road, Kiribathgoda', '0783456712', 'nimali.ds@gmail.com', '2001-08-30', 'FEMALE');

-- Seed Appointments
INSERT INTO appointments (appointment_no, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status, notes) VALUES
('APP-2026-001', 1, 1, 1, '2026-08-24', '09:00:00', 'SCHEDULED', 'Routine 6-month checkup'),
('APP-2026-002', 2, 4, 5, '2026-08-24', '10:30:00', 'SCHEDULED', 'Cosmetic laser whitening consultation'),
('APP-2026-003', 3, 1, 3, '2026-08-23', '14:00:00', 'COMPLETED', 'Severe molar tooth pain, root canal required'),
('APP-2026-004', 4, 2, 2, '2026-08-25', '11:00:00', 'SCHEDULED', 'Lower right premolar cavity filling');

-- Seed Initial Bill for Completed Appointment APP-2026-003
-- Consultation (3000) + Treatment (18000) = 21000, 5% Discount (1050), Total = 19950.00
INSERT INTO bills (bill_no, appointment_id, consultation_fee, treatment_cost, discount_rate, discount_amount, tax_rate, tax_amount, total_amount, payment_status, payment_method, bill_date) VALUES
('INV-2026-0003', 3, 3000.00, 18000.00, 5.00, 1050.00, 0.00, 0.00, 19950.00, 'PAID', 'CREDIT_CARD', '2026-08-23 15:00:00');

-- Seed Initial Audit Logs
INSERT INTO audit_logs (username, action, details) VALUES
('system', 'DATABASE_INITIALIZATION', 'Database schema created with seed data, triggers and stored procedures'),
('reception', 'APPOINTMENT_CREATE', 'Created appointment APP-2026-001 for Kamal Gunaratne'),
('admin', 'INVOICE_GENERATE', 'Generated Invoice INV-2026-0003 for Appointment APP-2026-003');
