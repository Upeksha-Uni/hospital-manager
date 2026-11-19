package com.example.hospital_manager.controller;

import com.example.hospital_manager.dto.RegisterRequest;
import com.example.hospital_manager.dto.UserResponse;
import com.example.hospital_manager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StaffController {

    private final AuthService authService;

    public StaffController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/api/doctors")
    public ResponseEntity<List<UserResponse>> getAllDoctors() {
        return ResponseEntity.ok(authService.getAllDoctors());
    }

    @PostMapping({"/api/doctors", "/api/admin/doctors"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> addDoctor(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.createDoctor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
