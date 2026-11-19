package com.example.hospital_manager.config;

import com.example.hospital_manager.model.AppUser;
import com.example.hospital_manager.model.Appointment;
import com.example.hospital_manager.model.AppointmentStatus;
import com.example.hospital_manager.model.MedicalRecord;
import com.example.hospital_manager.model.Patient;
import com.example.hospital_manager.model.Role;
import com.example.hospital_manager.repository.AppointmentRepository;
import com.example.hospital_manager.repository.MedicalRecordRepository;
import com.example.hospital_manager.repository.PatientRepository;
import com.example.hospital_manager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository,
                      PatientRepository patientRepository,
                      AppointmentRepository appointmentRepository,
                      MedicalRecordRepository medicalRecordRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        AppUser admin = new AppUser(null, "admin", passwordEncoder.encode("password123"), "Hospital Administrator", Role.ADMIN);
        AppUser doctor1 = new AppUser(null, "doctor1", passwordEncoder.encode("password123"), "Dr. Gregory House", Role.DOCTOR);
        AppUser doctor2 = new AppUser(null, "doctor2", passwordEncoder.encode("password123"), "Dr. John Watson", Role.DOCTOR);
        AppUser patientUser = new AppUser(null, "patient1", passwordEncoder.encode("password123"), "Alice Smith", Role.PATIENT);

        userRepository.save(admin);
        doctor1 = userRepository.save(doctor1);
        doctor2 = userRepository.save(doctor2);
        patientUser = userRepository.save(patientUser);

        Patient patient1 = new Patient(null, "Alice Smith", 29, "Female", "0771234567", "123 Galle Road, Colombo", patientUser);
        Patient patient2 = new Patient(null, "Bob Fernando", 45, "Male", "0712345678", "45 Kandy Road, Kelaniya", null);
        Patient patient3 = new Patient(null, "Clara Perera", 62, "Female", "0753456789", "78 High Level Road, Nugegoda", null);

        patient1 = patientRepository.save(patient1);
        patient2 = patientRepository.save(patient2);
        patient3 = patientRepository.save(patient3);

        LocalDateTime date1 = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime date2 = LocalDateTime.now().plusDays(3).withHour(14).withMinute(30).withSecond(0).withNano(0);

        Appointment appt1 = new Appointment(null, patient1, doctor1, date1, "Severe migraines and dizziness", AppointmentStatus.SCHEDULED);
        Appointment appt2 = new Appointment(null, patient2, doctor2, date2, "Routine cardiac checkup", AppointmentStatus.SCHEDULED);

        appt1 = appointmentRepository.save(appt1);
        appt2 = appointmentRepository.save(appt2);

        MedicalRecord record1 = new MedicalRecord(
            null,
            patient1,
            doctor1,
            appt1,
            "Tension headache and fatigue",
            "Paracetamol 500mg as needed, adequate hydration and rest",
            LocalDateTime.now().minusDays(1)
        );

        medicalRecordRepository.save(record1);
    }
}
