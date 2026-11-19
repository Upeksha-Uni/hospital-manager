package com.example.hospital_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicalRecordRequest(
    @NotNull Long patientId,
    Long appointmentId,
    @NotBlank String diagnosis,
    String prescription
) {
}
