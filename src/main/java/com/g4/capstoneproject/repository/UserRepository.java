package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
    Optional<User> findByPhone(String phone);
    
    /**
     * Tìm user theo email hoặc số điện thoại
     */
    Optional<User> findByEmailOrPhone(String email, String phone);
    
    /**
     * Tìm user theo provider và providerId (cho OAuth)
     */
    Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId);
    
    /**
     * Kiểm tra email đã tồn tại
     */
    boolean existsByEmail(String email);
    
    /**
     * Kiểm tra số điện thoại đã tồn tại
     */
    boolean existsByPhone(String phone);
}
