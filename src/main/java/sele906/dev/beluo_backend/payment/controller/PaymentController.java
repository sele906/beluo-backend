package sele906.dev.beluo_backend.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sele906.dev.beluo_backend.payment.polar.service.PolarService;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PolarService polarService;

    @PostMapping("/polar/checkout")
    public ResponseEntity<Map<String, String>> createPolarCheckout(Authentication auth) {

        String userId = auth.getName();

        String checkoutUrl = polarService.createStarterCreditCheckout(userId);

        return ResponseEntity.ok(Map.of(
                "checkoutUrl", checkoutUrl
        ));
    }
}
