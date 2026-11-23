package com.hospital.config;

import com.hospital.entity.*;
import com.hospital.enums.*;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * Seeds the database with an admin user, departments, and sample doctors
 * on first startup. Safe to run multiple times (checks before inserting).
 *
 * Active only on the "default" and "dev" Spring profiles.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository          userRepository;
    private final DepartmentRepository    departmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder         passwordEncoder;

    @Bean
    @Profile({"default", "dev"})
    public CommandLineRunner seedData() {
        return args -> {
            seedAdmin();
            seedDepartments();
            seedDoctors();
            seedSamplePatient();
            log.info("=== Data seeding complete ===");
        };
    }

    // ── Admin ─────────────────────────────────────────────────

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@hospital.com")) return;

        User admin = User.builder()
            .email("admin@hospital.com")
            .password(passwordEncoder.encode("Admin@1234"))
            .role(Role.ADMIN)
            .status(AccountStatus.ACTIVE)
            .authProvider(AuthProvider.LOCAL)
            .build();
        userRepository.save(admin);
        log.info("Admin seeded: admin@hospital.com / Admin@1234");
    }

    // ── Departments ───────────────────────────────────────────

    private void seedDepartments() {
        String[][] depts = {
            {"Cardiology",       "Heart and cardiovascular system"},
            {"Orthopedics",      "Bones, joints, and muscles"},
            {"Neurology",        "Nervous system disorders"},
            {"Pediatrics",       "Children's health"},
            {"Dermatology",      "Skin, hair, and nail conditions"},
            {"Ophthalmology",    "Eye care and vision"},
            {"ENT",              "Ear, nose, and throat"},
            {"General Medicine", "Primary and preventive care"},
            {"Gynecology",       "Women's health"},
            {"Psychiatry",       "Mental health and behavioural medicine"}
        };

        for (String[] d : depts) {
            if (!departmentRepository.existsByNameIgnoreCase(d[0])) {
                departmentRepository.save(
                    Department.builder().name(d[0]).description(d[1]).build()
                );
            }
        }
        log.info("Departments seeded");
    }

    // ── Sample doctors ────────────────────────────────────────

    private void seedDoctors() {
        seedDoctor("dr.sharma@hospital.com",  "Dr. Rajesh Sharma",  "EMP001",
                   "MBBS, MD (Cardiology)",     12, new BigDecimal("800"),  "Cardiology");
        seedDoctor("dr.patel@hospital.com",   "Dr. Priya Patel",    "EMP002",
                   "MBBS, MS (Orthopedics)",    8,  new BigDecimal("600"),  "Orthopedics");
        seedDoctor("dr.mehta@hospital.com",   "Dr. Arjun Mehta",    "EMP003",
                   "MBBS, DM (Neurology)",      15, new BigDecimal("1000"), "Neurology");
        seedDoctor("dr.gupta@hospital.com",   "Dr. Sunita Gupta",   "EMP004",
                   "MBBS, MD (Pediatrics)",     6,  new BigDecimal("500"),  "Pediatrics");
    }

    private void seedDoctor(String email, String name, String empId,
                            String qual, int exp, BigDecimal fee, String deptName) {
        if (userRepository.existsByEmail(email)) return;

        Department dept = departmentRepository.findByNameIgnoreCase(deptName).orElseThrow();

        User user = User.builder()
            .email(email)
            .password(passwordEncoder.encode("Doctor@1234"))
            .role(Role.DOCTOR)
            .status(AccountStatus.ACTIVE)
            .authProvider(AuthProvider.LOCAL)
            .build();
        userRepository.save(user);

        DoctorProfile profile = DoctorProfile.builder()
            .user(user)
            .fullName(name)
            .employeeId(empId)
            .phone("9876543210")
            .qualification(qual)
            .experienceYears(exp)
            .consultationFee(fee)
            .department(dept)
            .build();
        doctorProfileRepository.save(profile);
        log.info("Doctor seeded: {} ({})", email, deptName);
    }

    // ── Sample patient ────────────────────────────────────────

    private void seedSamplePatient() {
        if (userRepository.existsByEmail("patient@hospital.com")) return;

        User user = User.builder()
            .email("patient@hospital.com")
            .password(passwordEncoder.encode("Patient@1234"))
            .role(Role.PATIENT)
            .status(AccountStatus.ACTIVE)
            .authProvider(AuthProvider.LOCAL)
            .build();
        userRepository.save(user);

        PatientProfile profile = PatientProfile.builder()
            .user(user)
            .fullName("Sample Patient")
            .phone("9123456789")
            .bloodGroup("O+")
            .build();
        patientProfileRepository.save(profile);
        log.info("Sample patient seeded: patient@hospital.com / Patient@1234");
    }
}
