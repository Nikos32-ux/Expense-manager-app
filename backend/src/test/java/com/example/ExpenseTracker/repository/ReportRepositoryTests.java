package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.dto.CheckReportExistsDTO;
import com.example.ExpenseTracker.model.Report;
import com.example.ExpenseTracker.model.RoleCategory;
import com.example.ExpenseTracker.model.Roles;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.testsupport.PostgresTestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

@DataJpaTest
@Import(PostgresTestcontainersConfiguration.class)
public class ReportRepositoryTests{

    @Autowired
    EntityManager entityManager;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RolesRepository rolesRepository;

    Roles role;
    User savedUser;

    @BeforeEach
    void setUp(){
        role = rolesRepository.findByRoleType(RoleCategory.ROLE_USER).orElseThrow();

        savedUser = new User();
        savedUser.setUsername("Nikos");
        savedUser.setEmail("test_" + System.currentTimeMillis() + "@gmail.com");
        savedUser.setPassword("pass123!");
        savedUser.setImageProfile("img.jpg");
        savedUser.getRoles().add(role);

        userRepository.save(savedUser);
    }

    @Nested
    class findByUserId{

        @Test
        void shouldReturnReport_WhenReportExists(){
            Report savedReport = new Report();
            savedReport.setStatus("IDLE");
            savedReport.setUser(savedUser);
            savedReport.setChanged(false);
            savedReport.setCsvFile("file-123");
            savedReport.setWsSent(false);

            reportRepository.save(savedReport);

            Optional<Report> result =
                    reportRepository.findByUserId(savedUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getCsvFile()).isEqualTo("file-123");
        }

        @Test
        void shouldReturnEmpty_whenReportDoesNotExist(){
            Optional<Report> result =
                    reportRepository.findByUserId(savedUser.getId());

            assertThat(result).isNotPresent();
        }
    }


    @Nested
    class findStatusByUserId{
        @Test
        void shouldReturnReportDto_whenReportExistsAndHasReport(){
            Report savedReport = new Report();
            savedReport.setStatus("IDLE");
            savedReport.setUser(savedUser);
            savedReport.setChanged(false);
            savedReport.setCsvFile("file-123");
            savedReport.setWsSent(false);

            reportRepository.save(savedReport);

            Optional<Report> result =
                    reportRepository.findByUserId(savedUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getCsvFile()).isEqualTo("file-123");
        }

        @Test
        void shouldReturnEmpty_whenUserExistsButDoesNotHaveReport(){
            Optional<Report> result =
                    reportRepository.findByUserId(savedUser.getId());

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class getExistingReportData{

        @Test
        void shouldReturnReportData_whenReportExists() {
            Report savedReport = new Report();
            savedReport.setStatus("IDLE");
            savedReport.setUser(savedUser);
            savedReport.setChanged(false);
            savedReport.setCsvFile("file-123");
            savedReport.setWsSent(false);

            reportRepository.save(savedReport);

            Optional<CheckReportExistsDTO> result =
                    reportRepository.getExistingReportData(savedUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getFilePath()).isEqualTo("file-123");
            assertThat(result.get().getStatus()).isEqualTo("IDLE");
        }

        @Test
        void shouldReturnEmpty_whenUserExistsButDoesNotHaveReport() {
            Optional<CheckReportExistsDTO> result =
                    reportRepository.getExistingReportData(savedUser.getId());

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class markReportStale{

        @Test
        void shouldMarkIsChangedTrue_whenUserExistsAndHasReport(){
            Report savedReport = new Report();
            savedReport.setStatus("IDLE");
            savedReport.setUser(savedUser);
            savedReport.setChanged(false);
            savedReport.setCsvFile("file-123");
            savedReport.setWsSent(false);

            reportRepository.save(savedReport);

            reportRepository.markReportStale(savedUser.getId());

            entityManager.clear();

            Optional<Report> result =
                    reportRepository.findByUserId(savedUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().isChanged()).isEqualTo(true);
        }


    }

    @Nested
    class setReportStatus{

        @Test
        void shouldAllowOnlyOne_whenMultipleAttempts(){
            Report savedReport = new Report();
            savedReport.setStatus("DONE");
            savedReport.setUser(savedUser);
            savedReport.setChanged(false);
            savedReport.setCsvFile("file-123");
            savedReport.setWsSent(false);

            reportRepository.save(savedReport);

            Integer firstAttempt = reportRepository.setReportStatus(savedUser.getId());
            Integer secondAttempt = reportRepository.setReportStatus(savedUser.getId());

            assertThat(firstAttempt).isEqualTo(1);
            assertThat(secondAttempt).isEqualTo(0);
        }
    }

    @Nested
    class insertReport{
        @Test
        void shouldAllowInsertOnlyOne_whenMultipleAttempts(){
            Integer firstAttempt = reportRepository.insertReport(savedUser.getId());
            Integer secondAttempt = reportRepository.insertReport(savedUser.getId());

            assertThat(firstAttempt).isEqualTo(1);
            assertThat(secondAttempt).isEqualTo(0);

            Optional<Report> result =
                    reportRepository.findByUserId(savedUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getCsvFile()).isNull();
            assertThat(result.get().getStatus()).isEqualTo("CREATED");
            assertThat(result.get().isChanged()).isEqualTo(false);
        }
    }

}
