package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    List<Survey> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<Survey> findByShowOnLandingTrueAndIsActiveTrueOrderByDisplayOrderAsc();

    List<Survey> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT COUNT(s) FROM Survey s WHERE s.isActive = true")
    long countActiveSurveys();

    @Query("SELECT COALESCE(SUM(s.responseCount), 0) FROM Survey s")
    long sumTotalResponses();
}
