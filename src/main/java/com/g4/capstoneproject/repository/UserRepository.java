package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo email
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm user theo số điện thoại
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Tìm user theo email hoặc số điện thoại
     */
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    /**
     * Tìm user theo Google ID (cho OAuth)
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Kiểm tra email đã tồn tại
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra số điện thoại đã tồn tại
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Kiểm tra Google ID đã tồn tại
     */
    boolean existsByGoogleId(String googleId);

    /**
     * Tìm users theo role
     */
    List<User> findByRole(User.UserRole role);

    /**
     * Tìm users đang hoạt động theo role
     */
    List<User> findByRoleAndIsActiveTrue(User.UserRole role);

    /**
     * Tìm tất cả bệnh nhân
     */
    @Query("SELECT u FROM User u WHERE u.role = 'PATIENT'")
    List<User> findAllPatients();

    /**
     * Tìm tất cả bác sĩ đang hoạt động
     */
    @Query("SELECT u FROM User u WHERE u.role = 'DOCTOR' AND u.isActive = true")
    List<User> findAllActiveDoctors();

    /**
     * Tìm tất cả lễ tân đang hoạt động
     */
    @Query("SELECT u FROM User u WHERE u.role = 'RECEPTIONIST' AND u.isActive = true")
    List<User> findAllActiveReceptionists();

    /**
     * Tìm kiếm user theo tên (từ UserInfo) hoặc email
     * Sử dụng LEFT JOIN để truy vấn vào bảng user_info
     */
    @Query("SELECT u FROM User u LEFT JOIN u.userInfo ui WHERE ui.fullName LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Tìm user kèm theo thông tin cá nhân (eager fetch)
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userInfo WHERE u.id = :id")
    Optional<User> findByIdWithUserInfo(@Param("id") Long id);

    /**
     * Tìm tất cả users kèm theo thông tin cá nhân
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userInfo")
    List<User> findAllWithUserInfo();

    /**
     * Tìm bệnh nhân theo ID bác sĩ (từ treatment plans)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN TreatmentPlan tp ON tp.patient.id = u.id " +
            "WHERE tp.doctor.id = :doctorId AND u.role = 'PATIENT'")
    List<User> findPatientsByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * Tìm bệnh nhân đang được điều trị bởi bác sĩ
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN TreatmentPlan tp ON tp.patient.id = u.id " +
            "WHERE tp.doctor.id = :doctorId AND tp.status = 'ACTIVE' AND u.role = 'PATIENT'")
    List<User> findActivePatientsOfDoctor(@Param("doctorId") Long doctorId);

    /**
     * Tìm kiếm bệnh nhân theo keyword (tên, email, số điện thoại)
     */
    @Query("SELECT u FROM User u LEFT JOIN u.userInfo ui " +
            "WHERE u.role = 'PATIENT' AND (ui.fullName LIKE %:keyword% OR u.email LIKE %:keyword% OR u.phoneNumber LIKE %:keyword%)")
    List<User> searchPatients(@Param("keyword") String keyword);

    /**
     * Đếm số bệnh nhân của một bác sĩ
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
            "JOIN TreatmentPlan tp ON tp.patient.id = u.id " +
            "WHERE tp.doctor.id = :doctorId AND u.role = 'PATIENT'")
    long countPatientsByDoctorId(@Param("doctorId") Long doctorId);
}
