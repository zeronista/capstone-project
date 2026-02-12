package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.PatientHealthProfileRequest;
import com.g4.capstoneproject.entity.PatientHealthProfile;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.PatientHealthProfileRepository;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientHealthProfileService {

    private final PatientHealthProfileRepository healthProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<PatientHealthProfile> getByUserId(Long userId) {
        return healthProfileRepository.findByUserId(userId);
    }

    @Transactional
    public PatientHealthProfile upsertHealthProfile(Long userId, PatientHealthProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

        if (user.getRole() != User.UserRole.PATIENT) {
            throw new IllegalArgumentException("Chỉ hỗ trợ hồ sơ sức khỏe cho bệnh nhân");
        }

        PatientHealthProfile profile = healthProfileRepository.findByUserId(userId)
                .orElseGet(() -> PatientHealthProfile.builder().user(user).build());

        // Basic validation
        if (request.getHeightCm() != null) {
            double h = request.getHeightCm();
            if (h < 50 || h > 300) {
                throw new IllegalArgumentException("Chiều cao phải từ 50 đến 300 cm");
            }
            profile.setHeightCm(h);
        } else {
            profile.setHeightCm(null);
        }

        if (request.getWeightKg() != null) {
            double w = request.getWeightKg();
            if (w < 1 || w > 500) {
                throw new IllegalArgumentException("Cân nặng phải từ 1 đến 500 kg");
            }
            profile.setWeightKg(w);
        } else {
            profile.setWeightKg(null);
        }

        profile.setBloodType(request.getBloodType());
        profile.setAllergies(request.getAllergies());
        profile.setChronicDiseases(request.getChronicDiseases());

        PatientHealthProfile saved = healthProfileRepository.save(profile);
        log.info("Updated health profile for patient userId={}", userId);
        return saved;
    }
}

