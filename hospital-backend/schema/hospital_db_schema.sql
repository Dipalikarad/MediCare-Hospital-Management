

CREATE DATABASE hospital_db
    CHARACTER SET utf8mb4;

USE hospital_db;

CREATE TABLE department (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    CONSTRAINT pk_department PRIMARY KEY (id),
    CONSTRAINT uk_dept_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    auth_provider VARCHAR(10) NOT NULL,
    created_at DATETIME(6) NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE doctor_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    phone VARCHAR(15) NULL,
    qualification VARCHAR(300) NULL,
    experience_years INT NULL,
    consultation_fee DECIMAL(10,2) NULL,
    department_id BIGINT NOT NULL,
    CONSTRAINT pk_doctor_profile PRIMARY KEY (id),
    CONSTRAINT uk_doctor_user UNIQUE (user_id),
    CONSTRAINT uk_doctor_emp_id UNIQUE (employee_id),
    INDEX idx_doctor_dept (department_id),
    CONSTRAINT fk_doctor_profile_user FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_doctor_profile_department FOREIGN KEY (department_id)
        REFERENCES department (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE patient_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(15) NULL,
    gender VARCHAR(10) NULL,
    date_of_birth DATE NULL,
    blood_group VARCHAR(5) NULL,
    address VARCHAR(500) NULL,
    allergies VARCHAR(500) NULL,
    emergency_contact VARCHAR(15) NULL,
    CONSTRAINT pk_patient_profile PRIMARY KEY (id),
    CONSTRAINT uk_patient_user UNIQUE (user_id),
    CONSTRAINT fk_patient_profile_user FOREIGN KEY (user_id)
        REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE doctor_availability (
    id BIGINT NOT NULL AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    day_of_week INT NOT NULL,
    start_time TIME(6) NOT NULL,
    end_time TIME(6) NOT NULL,
    slot_duration_minutes INT NOT NULL,
    active BIT(1) NOT NULL,
    CONSTRAINT pk_doctor_availability PRIMARY KEY (id),
    INDEX idx_avail_doctor (doctor_id),
    CONSTRAINT fk_doctor_availability_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor_profile (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE appointment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME(6) NOT NULL,
    end_time TIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NULL,
    cancel_reason VARCHAR(500) NULL,
    created_at DATETIME(6) NULL,
    CONSTRAINT pk_appointment PRIMARY KEY (id),
    CONSTRAINT uk_appt_doctor_date_time
        UNIQUE (doctor_id, appointment_date, start_time),
    INDEX idx_appt_patient (patient_id),
    INDEX idx_appt_doctor (doctor_id),
    INDEX idx_appt_date (appointment_date),
    INDEX idx_appt_status (status),
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id)
        REFERENCES patient_profile (id),
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor_profile (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medical_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_id BIGINT NULL,
    diagnosis VARCHAR(1000) NULL,
    symptoms VARCHAR(1000) NULL,
    treatment_notes VARCHAR(2000) NULL,
    record_date DATE NOT NULL,
    CONSTRAINT pk_medical_record PRIMARY KEY (id),
    INDEX idx_mr_patient (patient_id),
    INDEX idx_mr_doctor (doctor_id),
    INDEX idx_mr_appointment (appointment_id),
    CONSTRAINT fk_medical_record_patient FOREIGN KEY (patient_id)
        REFERENCES patient_profile (id),
    CONSTRAINT fk_medical_record_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor_profile (id),
    CONSTRAINT fk_medical_record_appointment FOREIGN KEY (appointment_id)
        REFERENCES appointment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE password_reset_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token VARCHAR(200) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME(6) NOT NULL,
    CONSTRAINT pk_password_reset_token PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_token_token UNIQUE (token),
    INDEX idx_prt_token (token),
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id)
        REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prescription (
    id BIGINT NOT NULL AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    diagnosis VARCHAR(1000) NULL,
    advice VARCHAR(1000) NULL,
    created_at DATETIME(6) NULL,
    CONSTRAINT pk_prescription PRIMARY KEY (id),
    CONSTRAINT uk_prescription_appointment UNIQUE (appointment_id),
    CONSTRAINT fk_prescription_appointment FOREIGN KEY (appointment_id)
        REFERENCES appointment (id),
    CONSTRAINT fk_prescription_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor_profile (id),
    CONSTRAINT fk_prescription_patient FOREIGN KEY (patient_id)
        REFERENCES patient_profile (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prescription_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL,
    medicine_name VARCHAR(200) NOT NULL,
    dosage VARCHAR(100) NULL,
    frequency VARCHAR(100) NULL,
    duration_days INT NULL,
    instructions VARCHAR(500) NULL,
    CONSTRAINT pk_prescription_item PRIMARY KEY (id),
    INDEX idx_pi_prescription (prescription_id),
    CONSTRAINT fk_prescription_item_prescription FOREIGN KEY (prescription_id)
        REFERENCES prescription (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


