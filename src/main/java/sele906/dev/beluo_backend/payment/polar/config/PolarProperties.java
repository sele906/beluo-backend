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
    private String starterProductId;
    private String baseUrl;
    private String successUrl;
}
