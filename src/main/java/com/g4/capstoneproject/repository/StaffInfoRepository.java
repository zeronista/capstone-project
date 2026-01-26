package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.StaffInfo;
import com.g4.capstoneproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho StaffInfo entity
 */
@Repository
public interface StaffInfoRepository extends JpaRepository<StaffInfo, Long> {
    
    /**
     * Tìm thông tin nhân viên theo user
     */
    Optional<StaffInfo> findByUser(User user);
    
    /**
     * Tìm thông tin nhân viên theo user ID
     */
    Optional<StaffInfo> findByUserId(Long userId);
    
    /**
     * Tìm theo mã nhân viên
     */
    Optional<StaffInfo> findByEmployeeCode(String employeeCode);
    
    /**
     * Tìm theo số giấy phép hành nghề
     */
    Optional<StaffInfo> findByLicenseNumber(String licenseNumber);
    
    /**
     * Tìm theo phòng ban
     */
    List<StaffInfo> findByDepartment(String department);
    
    /**
     * Tìm theo chuyên khoa (cho bác sĩ)
     */
    List<StaffInfo> findBySpecialization(String specialization);
    
    /**
     * Tìm nhân viên đang hoạt động
     */
    List<StaffInfo> findByStatus(StaffInfo.StaffStatus status);
    
    /**
     * Tìm tất cả bác sĩ với thông tin chuyên khoa
     */
    @Query("SELECT s FROM StaffInfo s JOIN s.user u WHERE u.role = 'DOCTOR' AND s.status = 'ACTIVE'")
    List<StaffInfo> findAllActiveDoctorsWithInfo();
    
    /**
     * Kiểm tra mã nhân viên đã tồn tại
     */
    boolean existsByEmployeeCode(String employeeCode);
    
    /**
     * Kiểm tra số giấy phép đã tồn tại
     */
    boolean existsByLicenseNumber(String licenseNumber);
}
