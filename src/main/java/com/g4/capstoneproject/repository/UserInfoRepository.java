package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho UserInfo entity
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    
    /**
     * Tìm thông tin cá nhân theo user
     */
    Optional<UserInfo> findByUser(User user);
    
    /**
     * Tìm thông tin cá nhân theo user ID
     */
    Optional<UserInfo> findByUserId(Long userId);
    
    /**
     * Kiểm tra xem user đã có thông tin cá nhân chưa
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Tìm kiếm theo họ tên (không phân biệt hoa thường)
     */
    @Query("SELECT ui FROM UserInfo ui WHERE LOWER(ui.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UserInfo> searchByFullName(@Param("keyword") String keyword);
    
    /**
     * Xóa thông tin cá nhân theo user ID
     */
    void deleteByUserId(Long userId);
}
