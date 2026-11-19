package com.example.hospital_manager.repository;

import com.example.hospital_manager.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByFullNameContainingIgnoreCase(String name);

    Optional<Patient> findByUserUsername(String username);

    Optional<Patient> findByUserId(Long userId);
}
