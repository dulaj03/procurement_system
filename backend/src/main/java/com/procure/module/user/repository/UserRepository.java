package com.procure.module.user.repository;

import com.procure.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false AND u.status = 'ACTIVE'")
    Optional<User> findActiveByEmail(String email);
}
