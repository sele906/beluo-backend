package sele906.dev.beluo_backend.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sele906.dev.beluo_backend.credit.service.CreditService;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/polar/webhook")
public class PolarWebhookController {

    private final ObjectMapper objectMapper;

    public PolarWebhookController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    private CreditService creditService;

    @PostMapping
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String body,
            @RequestHeader Map<String, String> headers
    ) throws Exception {

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
}
