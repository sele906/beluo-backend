package sele906.dev.beluo_backend.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sele906.dev.beluo_backend.notification.rabbit.NotificationMessage;
import sele906.dev.beluo_backend.notification.rabbit.NotificationProducer;
import sele906.dev.beluo_backend.notification.rabbit.NotificationSyncSender;

import java.util.Map;

//메세지큐 측정용
@Profile("local")
@RestController
@RequestMapping("/api/test")
public class NotificationTestController {

    @Autowired
    private NotificationProducer notificationProducer;

    @Autowired
    private NotificationSyncSender notificationSyncSender;

    // 비동기 측정용
    @PostMapping("/measure/async")
    public Map<String, String> measureAsync(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String sessionId = body.get("sessionId");
        String reply = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

        notificationProducer.send(new NotificationMessage(userId, sessionId, reply));  // 큐 발행

        return Map.of("reply", reply);
    }

    // 동기 측정용
    @PostMapping("/measure/sync")
    public Map<String, String> measureSync(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String sessionId = body.get("sessionId");
        String reply = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

        notificationSyncSender.sendSync(new NotificationMessage(userId, sessionId, reply));  // 동기 발송

        return Map.of("reply", reply);
    }

}
