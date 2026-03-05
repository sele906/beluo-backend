package sele906.dev.beluo_backend.ai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
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
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.create()
                                        .responseTimeout(Duration.ofSeconds(20))
                        )
                )
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
                .block(Duration.ofSeconds(25));

        if (response == null) {
            throw new RuntimeException("OpenAI 응답이 없습니다.");
        }

        List<Map> choices = (List<Map>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("OpenAI choices가 없습니다.");
        }

        Map message = (Map) choices.get(0).get("message");

        return (String) message.get("content");
    }
}
