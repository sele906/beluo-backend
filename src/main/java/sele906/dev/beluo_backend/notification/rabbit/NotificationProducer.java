package sele906.dev.beluo_backend.notification.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.config.rabbit.RabbitConfig;

@Service
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(NotificationMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,   // 어느 exchange로
                RabbitConfig.ROUTING_KEY,     // 어떤 routing key로
                message                        // 객체 (자동으로 JSON 변환됨)
        );
    }
}
