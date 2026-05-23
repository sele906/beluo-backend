package sele906.dev.beluo_backend.credit.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "creditHistory")
public class CreditHistory {

    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private String paymentId;

    private String userId;
    private String type; // GRANT | USE | EXPIRE
    private int amount;
    private String source; // FREE_BETA | PAYMENT | CHAT | REGENERATE
    private Instant expiredAt;
    private boolean expired = false;
    private String memo;
    private Instant createdAt;
}
