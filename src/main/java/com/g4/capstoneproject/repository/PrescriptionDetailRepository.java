package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.PrescriptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho PrescriptionDetail entity
 */
@Repository
public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, Long> {
    
    /**
     * Tìm chi tiết theo đơn thuốc
     */
    List<PrescriptionDetail> findByPrescriptionId(Long prescriptionId);
    
    /**
     * Tìm theo tên thuốc
     */
    @Query("SELECT pd FROM PrescriptionDetail pd WHERE pd.medicineName LIKE %:medicineName%")
    List<PrescriptionDetail> findByMedicineNameContaining(@Param("medicineName") String medicineName);
    
    /**
     * Đếm số loại thuốc trong đơn
     */
    long countByPrescriptionId(Long prescriptionId);
    
    /**
     * Tìm thuốc được kê nhiều nhất
     */
    @Query("SELECT pd.medicineName, COUNT(pd) as count FROM PrescriptionDetail pd GROUP BY pd.medicineName ORDER BY count DESC")
    List<Object[]> findMostPrescribedMedicines();
}
