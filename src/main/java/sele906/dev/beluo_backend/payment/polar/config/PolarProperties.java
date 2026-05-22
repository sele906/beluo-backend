package sele906.dev.beluo_backend.payment.polar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "polar")
public class PolarProperties {
    private String accessToken;
    private String baseUrl;
    private String successUrl;
    private String webhookSecret;

    private Product product = new Product();

    @Getter
    @Setter
    public static class Product {
        private String credit100Id;
        private String credit350Id;
        private String credit650Id;
    }
}
