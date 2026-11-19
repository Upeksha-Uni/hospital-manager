package com.example.hospital_manager.dto;

import java.time.LocalDateTime;

public record MedicalRecordResponse(
    Long id,
    Long patientId,
    String patientName,
    Long doctorId,
    String doctorName,
    Long appointmentId,
    String diagnosis,
    String prescription,
    LocalDateTime createdAt
) {
}
