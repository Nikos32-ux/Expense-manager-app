package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.model.RoleCategory;
import com.example.ExpenseTracker.model.Roles;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.testsupport.PostgresTestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Import(PostgresTestcontainersConfiguration.class)
public class UserRepositoryTests{

    @Autowired
    UserRepository userRepository;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    EntityManager entityManager;


    Roles role;
    User savedUser;

    @Nested
    class findByEmail{

        @Test
        void shouldReturnUserWithRoles_whenEmailExists(){
            role = rolesRepository.findByRoleType(RoleCategory.ROLE_USER).orElseThrow();
            savedUser = new User();
            savedUser.setUsername("Nikos");
            savedUser.setEmail("test1@gmail.com");
            savedUser.setPassword("pass123!");
            savedUser.setImageProfile("img.jpg");
            savedUser.getRoles().add(role);

            userRepository.save(savedUser);

            entityManager.flush();
            entityManager.clear();

            Optional<User> result =
                    userRepository.findByEmail("test1@gmail.com");

            assertThat(result).isPresent();
            assertThat(result.get().getRoles()).isNotEmpty();
        }

        @Test
        void shouldReturnEmpty_whenEmailDoesNotExist(){
            Optional<User> result =
                    userRepository.findByEmail("test1@gmail.com");

            assertThat(result).isNotPresent();
        }

    }

    @Nested
    class findByEmailBasic{

        @Test
        void shouldReturnUser_whenUserExists(){
            role = rolesRepository.findByRoleType(RoleCategory.ROLE_USER).orElseThrow();
            savedUser = new User();
            savedUser.setUsername("Nikos");
            savedUser.setEmail("test1@gmail.com");
            savedUser.setPassword("pass123!");
            savedUser.setImageProfile("img.jpg");
            savedUser.getRoles().add(role);

            userRepository.save(savedUser);

            entityManager.flush();
            entityManager.clear();

            Optional<User> result =
                    userRepository.findByEmailBasic(savedUser.getId());
            boolean isLoaded = Hibernate.isInitialized(result.get().getRoles());
            assertThat(result).isPresent();
            assertThat(isLoaded).isFalse();
        }

        @Test
        void shouldReturnEmpty_whenUserDoesNotExist(){
            Optional<User> result =
                    userRepository.findByEmailBasic(999L);

            assertThat(result).isNotPresent();
        }
    }


}
