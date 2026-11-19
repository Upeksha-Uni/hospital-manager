package com.example.hospital_manager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AppointmentRequest(
    Long patientId,
    @NotNull Long doctorId,
    @NotNull @Future @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime appointmentDate,
    String reason
) {
}
