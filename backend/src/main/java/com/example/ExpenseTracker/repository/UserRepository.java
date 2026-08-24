package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.dto.AdminUserResponse;
import com.example.ExpenseTracker.dto.LoginResDTO;
import com.example.ExpenseTracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
               SELECT
                   u.id as id,
                   u.email as email,
                   u.created_at as timestamp
               FROM users u
            """, nativeQuery = true)
    Page<AdminUserResponse> getUsers(Pageable pageable);

    @Query(value = """
               SELECT
                   u.id as id,
                   u.email as email,
                   u.created_at as timestamp
               FROM users u
               WHERE u.email = :email
            """, nativeQuery = true)
    Optional<AdminUserResponse> getUser(@Param("email") String email);

    @Query(value = "SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByEmailBasic(@Param("userId") Long userId);
};
