package com.example.hospital_manager.service;

import com.example.hospital_manager.dto.MedicalRecordRequest;
import com.example.hospital_manager.dto.MedicalRecordResponse;
import com.example.hospital_manager.exception.ApiException;
import com.example.hospital_manager.model.AppUser;
import com.example.hospital_manager.model.Appointment;
import com.example.hospital_manager.model.MedicalRecord;
import com.example.hospital_manager.model.Patient;
import com.example.hospital_manager.model.Role;
import com.example.hospital_manager.repository.AppointmentRepository;
import com.example.hospital_manager.repository.MedicalRecordRepository;
import com.example.hospital_manager.repository.PatientRepository;
import com.example.hospital_manager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PatientRepository patientRepository,
                                AppointmentRepository appointmentRepository,
                                UserRepository userRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    public List<MedicalRecordResponse> getRecords(String currentUsername) {
        AppUser currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<MedicalRecord> records;

        if (currentUser.getRole() == Role.ADMIN) {
            records = medicalRecordRepository.findAll();
        } else if (currentUser.getRole() == Role.DOCTOR) {
            records = medicalRecordRepository.findByDoctorUsername(currentUsername);
        } else if (currentUser.getRole() == Role.PATIENT) {
            records = medicalRecordRepository.findByPatientUserUsername(currentUsername);
        } else {
            records = medicalRecordRepository.findAll();
        }

        List<MedicalRecordResponse> responses = new ArrayList<>();
        for (MedicalRecord r : records) {
            responses.add(toResponse(r));
        }
        return responses;
    }

    public MedicalRecordResponse getRecordById(Long id, String currentUsername) {
        MedicalRecord record = medicalRecordRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medical record not found with id: " + id));

        AppUser currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser.getRole() == Role.PATIENT) {
            if (record.getPatient().getUser() == null || !record.getPatient().getUser().getUsername().equals(currentUsername)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to view this medical record");
            }
        } else if (currentUser.getRole() == Role.DOCTOR) {
            if (!record.getDoctor().getUsername().equals(currentUsername)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to view this medical record");
            }
        }

        return toResponse(record);
    }

    @Transactional
    public MedicalRecordResponse createRecord(MedicalRecordRequest request, String currentDoctorUsername) {
        AppUser doctor = userRepository.findByUsername(currentDoctorUsername)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found"));

        if (doctor.getRole() != Role.DOCTOR && doctor.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only doctors can create medical records");
        }

        Patient patient = patientRepository.findById(request.patientId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found with id: " + request.patientId()));

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository.findById(request.appointmentId()).orElse(null);
        }

        MedicalRecord record = new MedicalRecord(
            null,
            patient,
            doctor,
            appointment,
            request.diagnosis(),
            request.prescription(),
            LocalDateTime.now()
        );

        MedicalRecord saved = medicalRecordRepository.save(record);
        return toResponse(saved);
    }

    private MedicalRecordResponse toResponse(MedicalRecord record) {
        Long appointmentId = record.getAppointment() != null ? record.getAppointment().getId() : null;
        return new MedicalRecordResponse(
            record.getId(),
            record.getPatient().getId(),
            record.getPatient().getFullName(),
            record.getDoctor().getId(),
            record.getDoctor().getFullName(),
            appointmentId,
            record.getDiagnosis(),
            record.getPrescription(),
            record.getCreatedAt()
        );
    }
}
