package com.ecommerce.payment_service.repository;

import com.ecommerce.payment_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
