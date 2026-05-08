package com.example.ExpenseTracker.service.report;
import com.example.ExpenseTracker.dto.TaskMessageDTO;
import com.example.ExpenseTracker.config.rabbitmqconfig.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendReportTask(Long userId, String email){
        TaskMessageDTO taskMessage = new TaskMessageDTO(userId, email);
        rabbitTemplate.convertAndSend(RabbitMQConfig.REPORT_QUEUE, taskMessage);
    }
}
