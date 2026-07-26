package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.dto.LoginResDTO;
import com.example.ExpenseTracker.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths ={"roles","roles.capabilities"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByEmailBasic(@Param("userId") Long userId);
};
