package sele906.dev.beluo_backend.payment.polar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.payment.polar.client.PolarClient;
import sele906.dev.beluo_backend.payment.polar.config.PolarProperties;

@Service
public class PolarService {

    private final PolarClient polarClient;
    private final PolarProperties polarProperties;

    public PolarService(PolarClient polarClient, PolarProperties polarProperties) {
        this.polarClient = polarClient;
        this.polarProperties = polarProperties;
    }

    public String createCreditCheckout(String userId, String packageKey) {
        CreditProduct product = getCreditProduct(packageKey);

        PolarClient.PolarCheckoutResponse response =
                polarClient.createCheckout(
                        product.productId(),
                        polarProperties.getSuccessUrl(),
                        userId,
                        product.creditAmount()
                );

        if (response == null || response.url() == null || response.url().isBlank()) {
            throw new IllegalStateException("Polar checkout URL을 생성하지 못했습니다.");
        }

        return response.url();
    }

    private CreditProduct getCreditProduct(String packageKey) {
        return switch (packageKey) {
            case "CREDIT_100" -> new CreditProduct(
                    polarProperties.getProduct().getCredit100Id(),
                    100
            );
            case "CREDIT_350" -> new CreditProduct(
                    polarProperties.getProduct().getCredit350Id(),
                    350
            );
            case "CREDIT_650" -> new CreditProduct(
                    polarProperties.getProduct().getCredit650Id(),
                    650
            );
            default -> throw new IllegalArgumentException("지원하지 않는 크레딧 상품입니다.");
        };
    }

    private record CreditProduct(String productId, int creditAmount) {
    }
}
