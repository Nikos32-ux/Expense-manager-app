package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.testsupport.AbstractPostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
public class UserRepositoryTests extends AbstractPostgresTestContainer {

    @Autowired
    UserRepository userRepository;

    @Nested
    class createIfNotExists{
        @Test
        void shouldReturnNumberOneForSuccess_whenUserIsInsert(){
           Integer result =
                   userRepository.createIfNotExists("User1","test@gmail.com","pass123!");
           assertThat(result).isEqualTo(1);
        }

        @Test
        void shouldReturnNumberZero_whenUserIsNotInserted(){

           Integer savedUser =
                   userRepository.createIfNotExists("SavedUser","test@gmail.com","pass123!");

            Integer result =
                    userRepository.createIfNotExists("DuplicateUser","test@gmail.com","pass123!");
            assertThat(result).isEqualTo(0);
        }
    }

}
