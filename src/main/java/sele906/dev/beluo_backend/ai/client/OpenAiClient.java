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
            "model", "gpt-5-mini",
            "max_completion_tokens", 1500,
            "reasoning_effort", "low",
            "messages", messages
        );

        Map response = callOpenAi(body);

        List<Map> choices = (List<Map>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new AiResponseException("OpenAI 응답을 확인할 수 없어요. 잠시 후 다시 시도해 주세요");
        }

        Map message = (Map) choices.get(0).get("message");

        return (String) message.get("content");
    }

    public String summary(List<Map<String, String>> messages) {

        Map<String, Object> body = Map.of(
                "model", "gpt-5-mini",
                "max_completion_tokens", 2000,
                "reasoning_effort", "low",
                "messages", messages
        );

        Map response = callOpenAi(body);

        List<Map> choices = (List<Map>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new AiResponseException("OpenAI 응답을 확인할 수 없어요. 잠시 후 다시 시도해 주세요");
        }

        Map message = (Map) choices.get(0).get("message");

        return (String) message.get("content");
    }

    public String personality(Map<String, String> personality) {

        Map<String, Object> body = Map.of(
                "model", "gpt-5-mini",
                "max_completion_tokens", 2000,
                "reasoning_effort", "low",
                "messages", List.of(personality)
        );

        Map response = callOpenAi(body);

        List<Map> choices = (List<Map>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new AiResponseException("OpenAI 응답을 확인할 수 없어요. 잠시 후 다시 시도해 주세요");
        }

        Map message = (Map) choices.get(0).get("message");

        return (String) message.get("content");
    }

    private Map callOpenAi(Map<String, Object> body) {
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
        } catch (WebClientRequestException | IllegalStateException e) {
            throw new AiResponseException("AI 응답 시간이 초과됐어요. 잠시 후 다시 시도해 주세요.");
        }
    }
}
