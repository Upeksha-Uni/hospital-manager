package com.example.hospital_manager.service;

import com.example.hospital_manager.dto.AppointmentRequest;
import com.example.hospital_manager.dto.AppointmentResponse;
import com.example.hospital_manager.exception.ApiException;
import com.example.hospital_manager.model.AppUser;
import com.example.hospital_manager.model.Appointment;
import com.example.hospital_manager.model.AppointmentStatus;
import com.example.hospital_manager.model.Patient;
import com.example.hospital_manager.model.Role;
import com.example.hospital_manager.repository.AppointmentRepository;
import com.example.hospital_manager.repository.PatientRepository;
import com.example.hospital_manager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    public List<AppointmentResponse> getAppointments(String currentUsername) {
        AppUser currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<Appointment> appointments;

        if (currentUser.getRole() == Role.ADMIN) {
            appointments = appointmentRepository.findAll();
        } else if (currentUser.getRole() == Role.DOCTOR) {
            appointments = appointmentRepository.findByDoctorUsername(currentUsername);
        } else if (currentUser.getRole() == Role.PATIENT) {
            appointments = appointmentRepository.findByPatientUserUsername(currentUsername);
        } else {
            appointments = appointmentRepository.findAll();
        }

        List<AppointmentResponse> responses = new ArrayList<>();
        for (Appointment a : appointments) {
            responses.add(toResponse(a));
        }
        return responses;
    }

    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request, String currentUsername) {
        AppUser currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Patient patient;

        if (currentUser.getRole() == Role.PATIENT) {
            patient = patientRepository.findByUserUsername(currentUsername)
                .orElseGet(() -> {
                    Patient p = new Patient(null, currentUser.getFullName(), null, null, null, null, currentUser);
                    return patientRepository.save(p);
                });
        } else {
            if (request.patientId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Patient ID is required");
            }
            patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found with id: " + request.patientId()));
        }

        AppUser doctor = userRepository.findById(request.doctorId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found with id: " + request.doctorId()));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selected user is not a doctor");
        }

        Appointment appointment = new Appointment(
            null,
            patient,
            doctor,
            request.appointmentDate(),
            request.reason(),
            AppointmentStatus.SCHEDULED
        );

        Appointment saved = appointmentRepository.save(appointment);
        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus status, String currentUsername) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + id));

        AppUser currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser.getRole() == Role.DOCTOR) {
            if (!appointment.getDoctor().getUsername().equals(currentUsername)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "You can only update your own appointments");
            }
        }

        appointment.setStatus(status);
        Appointment saved = appointmentRepository.save(appointment);
        return toResponse(saved);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getPatient().getId(),
            appointment.getPatient().getFullName(),
            appointment.getDoctor().getId(),
            appointment.getDoctor().getFullName(),
            appointment.getAppointmentDate(),
            appointment.getReason(),
            appointment.getStatus()
        );
    }
}
