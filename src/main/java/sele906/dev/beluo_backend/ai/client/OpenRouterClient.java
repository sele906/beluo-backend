package sele906.dev.beluo_backend.ai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import sele906.dev.beluo_backend.ai.prompt.dto.PromptData;
import sele906.dev.beluo_backend.exception.AiResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class OpenRouterClient {

    private final WebClient webClient;

    private static final List<String> FALLBACK_MODELS = List.of(
            "openai/gpt-oss-120b:free",
            "meta-llama/llama-3.3-70b-instruct:free",
            "openrouter/free"
    );

    public OpenRouterClient(@Value("${openrouter.api.key}") String openRouterKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Authorization", "Bearer " + openRouterKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("HTTP-Referer", "https://beluo.site")
                .defaultHeader("X-Title", "beluo")
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.create()
                                        .responseTimeout(Duration.ofSeconds(30))
                        )
                )
                .build();
    }

    public String freeChat(PromptData promptData) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.addAll(promptData.getSystemMessages());
        messages.addAll(promptData.getRecentMessages());

        RuntimeException lastException = null;

        int maxRetries = 5;
        long delay = 1000; // 1초 시작

        for (int attempt = 0; attempt < maxRetries; attempt++) {

            List<String> shuffled = new ArrayList<>(FALLBACK_MODELS);
            Collections.shuffle(shuffled);

            for (String model : shuffled) {
                try {
                    return callModel(model, messages);

                } catch (RuntimeException e) {
                    String msg = e.getMessage();

                    if (msg != null && (msg.contains("429") || msg.contains("404"))) {
                        lastException = e; // 다음 모델 시도
                    } else {
                        throw e; // 다른 에러는 바로 터트림
                    }
                }
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry 중 인터럽트 발생", ie);
            }

            delay *= 2;
        }

        throw new AiResponseException("지금 사용자가 많아서 응답이 지연되고 있어요.");
    }

    private String callModel(String model, List<Map<String, String>> messages) {

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 1000,
                "temperature", 0.7,
                "messages", messages
        );

        Map response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(err -> new RuntimeException(err))
                )
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(35));

        if (response == null) {
            throw new RuntimeException("응답이 없습니다.");
        }

        List<Map> choices = (List<Map>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("choices가 없습니다.");
        }

        Map message = (Map) choices.get(0).get("message");

        if (message == null) {
            throw new RuntimeException("message 없음");
        }

        return (String) message.get("content");
    }
}
