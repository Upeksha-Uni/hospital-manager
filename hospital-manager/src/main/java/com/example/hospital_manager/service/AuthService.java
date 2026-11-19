package com.example.hospital_manager.service;

import com.example.hospital_manager.dto.AuthResponse;
import com.example.hospital_manager.dto.LoginRequest;
import com.example.hospital_manager.dto.RegisterRequest;
import com.example.hospital_manager.dto.UserResponse;
import com.example.hospital_manager.exception.ApiException;
import com.example.hospital_manager.model.AppUser;
import com.example.hospital_manager.model.Patient;
import com.example.hospital_manager.model.Role;
import com.example.hospital_manager.repository.PatientRepository;
import com.example.hospital_manager.repository.UserRepository;
import com.example.hospital_manager.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PatientRepository patientRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists: " + request.username());
        }

        Role role = request.role();
        if (role == null) {
            role = Role.PATIENT;
        }

        if (role != Role.PATIENT) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only administrators can register staff accounts");
            }
        }

        String fullName = request.getEffectiveFullName();
        AppUser user = new AppUser(null, request.username(), passwordEncoder.encode(request.password()), fullName, role);
        AppUser savedUser = userRepository.save(user);

        if (role == Role.PATIENT) {
            Patient patient = new Patient(null, fullName, null, null, null, null, savedUser);
            patientRepository.save(patient);
        }

        return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getFullName(), savedUser.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        AppUser user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(
            token,
            "Bearer",
            jwtService.getExpirationMs(),
            user.getUsername(),
            user.getFullName(),
            user.getRole().name()
        );
    }

    public UserResponse getCurrentUser(String username) {
        AppUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
    }

    public List<UserResponse> getAllUsers() {
        List<AppUser> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();
        for (AppUser u : users) {
            responses.add(new UserResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole()));
        }
        return responses;
    }

    public List<UserResponse> getAllDoctors() {
        List<AppUser> doctors = userRepository.findByRole(Role.DOCTOR);
        List<UserResponse> responses = new ArrayList<>();
        for (AppUser d : doctors) {
            responses.add(new UserResponse(d.getId(), d.getUsername(), d.getFullName(), d.getRole()));
        }
        return responses;
    }

    @Transactional
    public UserResponse createDoctor(RegisterRequest request) {
        RegisterRequest doctorRequest = new RegisterRequest(
            request.username(),
            request.password(),
            request.fullName(),
            request.name(),
            Role.DOCTOR
        );
        return register(doctorRequest);
    }
}
