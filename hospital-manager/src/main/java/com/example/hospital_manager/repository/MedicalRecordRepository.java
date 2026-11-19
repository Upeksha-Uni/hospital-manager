package com.example.hospital_manager.repository;

import com.example.hospital_manager.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientId(Long patientId);

    List<MedicalRecord> findByPatientUserUsername(String username);

    List<MedicalRecord> findByDoctorUsername(String username);

    boolean existsByPatientId(Long patientId);
}
