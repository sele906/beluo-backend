package sele906.dev.beluo_backend.ai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import sele906.dev.beluo_backend.ai.prompt.dto.PromptData;
import sele906.dev.beluo_backend.exception.AiResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GroqClient {

    private final WebClient webClient;

    public GroqClient(@Value("${groq.api.key}") String groqKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + groqKey)
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.create()
                                        .responseTimeout(Duration.ofSeconds(35))
                        )
                )
                .build();
    }

    public String chat(PromptData promptData) {

        List<Map<String, String>> recentMessages = new ArrayList<>(promptData.getRecentMessages());
        while (!recentMessages.isEmpty() && "assistant".equals(recentMessages.get(recentMessages.size() - 1).get("role"))) {
            recentMessages.remove(recentMessages.size() - 1);
        }
        if (recentMessages.isEmpty()) {
            throw new AiResponseException("대화 내용을 불러올 수 없어요. 잠시 후 다시 시도해 주세요.");
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.addAll(promptData.getSystemMessages());
        messages.addAll(recentMessages);

        Map<String, Object> body = Map.of(
                "model", "qwen/qwen3-32b",
                "messages", messages,
                "max_completion_tokens", 1500,
                "reasoning_effort", "none",
                "reasoning_format", "hidden",
                "temperature", 0.7,
                "top_p", 0.8
        );

        Map response = callGroq(body);

        List<Map> choices = (List<Map>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new AiResponseException("AI 응답을 확인할 수 없어요. 잠시 후 다시 시도해 주세요");
        }

        Map message = (Map) choices.get(0).get("message");

        if (message == null || message.get("content") == null) {
            throw new AiResponseException("AI 응답 내용을 확인할 수 없어요. 잠시 후 다시 시도해 주세요.");
        }

        return (String) message.get("content");
    }

    private Map callGroq(Map<String, Object> body) {
        try {
            Map response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(25));

            if (response == null) {
                throw new AiResponseException("AI 응답 시간이 초과됐어요. 잠시 후 다시 시도해 주세요.");
            }
            return response;
        } catch (WebClientResponseException e) {
            throw new AiResponseException("AI 요청 처리 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요.");

        } catch (WebClientRequestException | IllegalStateException e) {
            throw new AiResponseException("AI 응답 시간이 초과됐어요. 잠시 후 다시 시도해 주세요.");
        }
    }
}
