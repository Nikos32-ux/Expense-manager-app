package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.model.RoleCategory;
import com.example.ExpenseTracker.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Roles, Long> {
    Optional<Roles> findByRoleType(RoleCategory roletype);
}
