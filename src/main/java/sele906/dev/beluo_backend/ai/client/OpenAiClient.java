package sele906.dev.beluo_backend.ai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

//OpenAI API 연결하는 역할

@Component
public class OpenAiClient {

    private final WebClient webClient;

    public OpenAiClient(@Value("${openai.api.key}") String openAiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + openAiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String chat(List<Map<String, String>> messages) {

        Map<String, Object> body = Map.of(
                "model", "gpt-4.1-mini",
                "temperature", 0.5,
                "max_tokens", 150,
                "messages", messages
        );

        Map response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");

        return (String) message.get("content");
    }
}
