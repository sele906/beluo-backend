package sele906.dev.beluo_backend.payment.polar.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import sele906.dev.beluo_backend.payment.polar.config.PolarProperties;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PolarClient {

    private final WebClient webClient;

    public PolarClient(
            PolarProperties polarProperties
    ) {
        log.info("Polar baseUrl={}", polarProperties.getBaseUrl());
        log.info("Polar productId={}", polarProperties.getStarterProductId());
        log.info("Polar successUrl={}", polarProperties.getSuccessUrl());

        this.webClient = WebClient.builder()
                .baseUrl(polarProperties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + polarProperties.getAccessToken())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public PolarCheckoutResponse createCheckout(
            String productId,
            String successUrl,
            String userId
    ) {
        Map<String, Object> body = Map.of(
                "products", List.of(productId),
                "success_url", successUrl,
                "external_customer_id", String.valueOf(userId),
                "metadata", Map.of(
                        "userId", String.valueOf(userId),
                        "creditAmount", "300"
                )
        );

        return webClient.post()
                .uri("/checkouts/")
                .bodyValue(body)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(errorBody -> new RuntimeException("Polar API error: " + errorBody))
                )
                .bodyToMono(PolarCheckoutResponse.class)
                .block();
    }

    public record PolarCheckoutResponse(
            String id,
            String url,
            String status
    ) {
    }
}
