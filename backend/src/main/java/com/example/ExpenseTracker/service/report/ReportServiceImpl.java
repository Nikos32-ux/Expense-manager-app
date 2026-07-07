package com.example.ExpenseTracker.service.report;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.exception.ReportNotFoundException;
import com.example.ExpenseTracker.model.Notification;
import com.example.ExpenseTracker.model.Report;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.config.rabbitmqconfig.RabbitMQConfig;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.repository.NotificationRepository;
import com.example.ExpenseTracker.repository.ReportRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ExpenseRepository expenseRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationRepository notificationRepository;
    private final ReportMessageProducer reportMessageProducer;


    @Override
    @RabbitListener(queues = RabbitMQConfig.REPORT_QUEUE)
    @CacheEvict(value = "reportData", key = "#taskMessage.userId")
    public void createReport (TaskMessageDTO taskMessage){
        Long userId = taskMessage.userId();
        String email = taskMessage.email();

            Optional<ReportStatusProjection> reportStatus = reportRepository
                    .findStatusByUserId(userId);

            if(reportStatus.isPresent()){
                if(reportStatus.get().getStatus().equals("DONE")){
                    log.info("Report already finished for user {}. Skipping duplicate.", userId);
                    return;
                }
            }

            User userRef = userRepository.getReferenceById(userId);
            List<ExpenseReportDTO> list = getData(userId);
            Path file = createFile(list, userId);
            String file_url = cloudinaryService.updloadCsvFile(file, userId);

            updateOrCreate( userRef, file_url, email);

            try{
                Files.deleteIfExists(file);
            }catch(IOException e){
                log.warn("Failed to delete temp file {}", file);
            }

        }

    @Override
    @Cacheable(value = "reportData", key = "#userId" , unless = "#result == null")
    public ReportActionResponse getReportData(Long userId) {
        log.info("Entered getReportData {}", userId);
        Optional<CheckReportExistsDTO> existingReportData =
                reportRepository.getExistingReportData(userId);
        if(existingReportData.isPresent()){
            boolean isBusy = existingReportData.get().getStatus().equals("PROCESSING") || existingReportData.get().getStatus().equals("CREATED");;
            boolean isDone = existingReportData.get().getStatus().equals("DONE");

            if(isBusy) {
                log.info("Busy {}", userId);
                return new ReportActionResponse(
                        "IN_PROGRESS",
                       null,
                        LocalDateTime.now()
                );
            }

            if(isDone && !existingReportData.get().getIsChanged()){
                log.info("FRESH {}", userId);
                return new ReportActionResponse(
                        "FRESH",
                        existingReportData.get().getFilePath(),
                        LocalDateTime.now()
                );
            }else if(isDone && existingReportData.get().getIsChanged()){
                log.info("STALE {}", userId);
                return new ReportActionResponse(
                        "STALE",
                        existingReportData.get().getFilePath(),
                        LocalDateTime.now()
                );
            }

        }
        return null;
    }

    @Override
    public void createIfNeeded(ReportActionResponse existingReportData, Long userId, String email){
        if(existingReportData == null){
            log.info("Report CREATED userId={}", userId);
            int insertSucceeded = reportRepository.insertReport(userId);
            if(insertSucceeded == 1) {
                log.info("Report message sent userId={}", userId);
                reportMessageProducer.sendReportTask(userId, email);
            }
        }

        if(existingReportData != null && existingReportData.status().equals("STALE")){
            log.info("report PROCESSING userId {}", userId);
            int updateSucceeded = reportRepository.setReportStatus(userId);
            if(updateSucceeded == 1){
                log.info("report message sent userId {}", userId);
                reportMessageProducer.sendReportTask(userId, email);
            }
        }
    }

    @Transactional
    public void updateOrCreate(User userRef, String file_url, String email){
        Report report = reportRepository.findByUserId(userRef.getId())
                .orElseThrow(() -> new ReportNotFoundException("Report missing"));
            report.setCsvFile(file_url);
            report.setChanged(false);
            report.setStatus("DONE");
            report.setUser(userRef);

            reportRepository.save(report);

            Notification notification = new Notification();

            notification.setSentAt(LocalDateTime.now());
            notification.setCsvFile(file_url);
            notification.setType("FILE_GENERATED");
            notification.setUser(userRef);

            notificationRepository.save(notification);

            simpMessagingTemplate.convertAndSendToUser(
                    email,
                    "topic/notifications",
                    new SendReportWebSocketDTO(
                            notification.getId(),
                            notification.getSentAt(),
                            notification.getCsvFile(),
                            notification.getType(),
                            notification.isRead()
            ));
    }



    private List<ExpenseReportDTO> getData(Long userId){
        return expenseRepository.findReportByUserId(userId);
    }


    private Path createFile(List<ExpenseReportDTO> list, Long userId){
        Path tempFile;
        Path finalFile;
        try{
            tempFile = Path.of("temp" + userId + ".temp");
            finalFile = Path.of("final" + userId + ".csv");
            if(Files.exists(finalFile)){
                return finalFile;
            }
            try(BufferedWriter bw =  Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)){
                bw.write("Amount,Description,Date,Month,Year,Payment,Category,Expense_Level");
                bw.newLine();
                for(ExpenseReportDTO row : list){
                    bw.write(
                            row.getAmount() + "," +
                                    row.getDescription() + "," +
                                    row.getDate() + "," +
                                    row.getMonth() + "," +
                                    row.getYear() + "," +
                                    row.getPayment() + "," +
                                    row.getCategory() + "," +
                                    row.getExpenseLevel()
                    );
                    bw.newLine();
                }

                return Files.move(tempFile, finalFile, StandardCopyOption.ATOMIC_MOVE);
            }
        }catch (IOException e){
            throw new RuntimeException("no finished file");
        }
    }

}
