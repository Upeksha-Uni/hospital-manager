package com.example.hospital_manager.service;

import com.example.hospital_manager.exception.ApiException;
import com.example.hospital_manager.model.AppUser;
import com.example.hospital_manager.model.Patient;
import com.example.hospital_manager.model.Role;
import com.example.hospital_manager.repository.AppointmentRepository;
import com.example.hospital_manager.repository.MedicalRecordRepository;
import com.example.hospital_manager.repository.PatientRepository;
import com.example.hospital_manager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          MedicalRecordRepository medicalRecordRepository,
                          UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository = userRepository;
    }

    public List<Patient> getAllPatients(String currentUsername) {
        AppUser currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser.getRole() == Role.PATIENT) {
            return patientRepository.findByUserUsername(currentUsername)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
        }

        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id, String currentUsername) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found with id: " + id));

        AppUser currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser.getRole() == Role.PATIENT) {
            if (patient.getUser() == null || !patient.getUser().getUsername().equals(currentUsername)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to view this patient's details");
            }
        }

        return patient;
    }

    @Transactional
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient updatePatient(Long id, Patient updatedPatient, String currentUsername) {
        Patient patient = getPatientById(id, currentUsername);

        AppUser currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser.getRole() == Role.PATIENT) {
            if (patient.getUser() == null || !patient.getUser().getUsername().equals(currentUsername)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to update this patient");
            }
        }

        patient.setFullName(updatedPatient.getFullName());
        patient.setAge(updatedPatient.getAge());
        patient.setGender(updatedPatient.getGender());
        patient.setContactNumber(updatedPatient.getContactNumber());
        patient.setAddress(updatedPatient.getAddress());

        return patientRepository.save(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Patient not found with id: " + id);
        }

        if (appointmentRepository.existsByPatientId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete patient with associated appointments");
        }

        if (medicalRecordRepository.existsByPatientId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete patient with associated medical records");
        }

        patientRepository.deleteById(id);
    }
}
