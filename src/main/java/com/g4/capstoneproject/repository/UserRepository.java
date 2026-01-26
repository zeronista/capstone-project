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
     * Tìm kiếm user theo tên hoặc email
     */
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);
}
