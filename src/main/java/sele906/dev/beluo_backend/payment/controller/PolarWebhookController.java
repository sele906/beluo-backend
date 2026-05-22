package sele906.dev.beluo_backend.payment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.standardwebhooks.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.credit.service.CreditService;
import sele906.dev.beluo_backend.payment.polar.config.PolarProperties;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/payment/polar/webhook")
public class PolarWebhookController {

    private final ObjectMapper objectMapper;

    public PolarWebhookController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    private CreditService creditService;

    @Autowired
    private PolarProperties polarProperties;

    @PostMapping
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String body,
            @RequestHeader HttpHeaders headers
    ) throws Exception {

        verifyPolarWebhook(body, headers);

        JsonNode root = objectMapper.readTree(body);

        String type = root.path("type").asText();

        if (!"order.paid".equals(type)) {
            return ResponseEntity.ok("ignored");
        }

        JsonNode data = root.path("data");

        String orderId = data.path("id").asText();
        boolean paid = data.path("paid").asBoolean(false);
        String status = data.path("status").asText();

        if (!paid || !"paid".equals(status)) {
            return ResponseEntity.ok("not paid");
        }

        JsonNode metadata = data.path("metadata");

        String userId = metadata.path("userId").asText();
        int creditAmount = metadata.path("creditAmount").asInt();

        creditService.grantPaymentCredits(userId, creditAmount, orderId);

        return ResponseEntity.ok("ok");
    }

    private void verifyPolarWebhook(String body, HttpHeaders springHeaders) {
        String secret = polarProperties.getWebhookSecret();

        try {
            new Webhook(secret.getBytes(StandardCharsets.UTF_8))
                    .verify(body, springHeaders);
            return;

        } catch (Exception e) {
            log.warn("Polar webhook 실패: {}", e.getMessage());
        }

        throw new IllegalArgumentException("유효하지 않은 Polar webhook 요청입니다.");
    }
}
