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

    public String createStarterCreditCheckout(String userId) {
        PolarClient.PolarCheckoutResponse response =
                polarClient.createCheckout(
                        polarProperties.getStarterProductId(),
                        polarProperties.getSuccessUrl(),
                        userId
                );

        return response.url();
    }
}
