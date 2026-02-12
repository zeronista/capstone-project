package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.PatientHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientHealthProfileRepository extends JpaRepository<PatientHealthProfile, Long> {

    Optional<PatientHealthProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}

