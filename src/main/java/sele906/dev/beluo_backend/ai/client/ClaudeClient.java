package sele906.dev.beluo_backend.ai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import sele906.dev.beluo_backend.ai.prompt.dto.PromptData;
import sele906.dev.beluo_backend.exception.AiResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ClaudeClient {

    private final WebClient webClient;

    public ClaudeClient(@Value("${claude.api.key}") String claudeKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com/v1")
                .defaultHeader("x-api-key", claudeKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.create()
                                        .responseTimeout(Duration.ofSeconds(30))
                        )
                )
                .build();
    }

    public String chat(PromptData promptData) {

        // Claude는 system을 별도 파라미터로, messages에는 user/assistant만
        String systemContent = promptData.getSystemMessages().stream()
                .map(m -> m.get("content"))
                .collect(Collectors.joining("\n\n"));

        Map<String, Object> body = new HashMap<>();
        body.put("model", "claude-sonnet-4-6");
        body.put("max_tokens", 1500);
        body.put("system", systemContent);
        body.put("messages", promptData.getRecentMessages());

        Map response;
        try {
            response = webClient.post()
                    .uri("/messages")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res ->
                            res.bodyToMono(String.class)
                                    .map(err -> new RuntimeException(err))
                    )
                    .bodyToMono(Map.class)
                    .doOnError(e -> {
                        if (e instanceof WebClientResponseException ex) {
                            System.out.println("API 에러 상태코드: " + ex.getStatusCode());
                            System.out.println("API 에러 바디: " + ex.getResponseBodyAsString());
                        } else {
                            System.out.println("API 에러: " + e.getMessage());
                        }
                    })
                    .block(Duration.ofSeconds(35));
        } catch (WebClientRequestException | IllegalStateException e) {
            throw new AiResponseException("AI 응답 시간이 초과됐어요. 잠시 후 다시 시도해 주세요.");
        }

        if (response == null) {
            throw new AiResponseException("AI 응답 시간이 초과됐어요. 잠시 후 다시 시도해 주세요.");
        }

        // Claude 응답 구조: { "content": [{ "type": "text", "text": "..." }] }
        List<Map> content = (List<Map>) response.get("content");

        if (content == null || content.isEmpty()) {
            throw new AiResponseException("Claude content가 없습니다.");
        }

        return (String) content.get(0).get("text");
    }
}
