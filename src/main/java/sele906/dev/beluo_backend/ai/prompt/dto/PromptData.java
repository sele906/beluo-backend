package sele906.dev.beluo_backend.ai.prompt.dto;

import java.util.List;
import java.util.Map;

public class PromptData {

    private final List<Map<String, String>> systemMessages;
    private final List<Map<String, String>> recentMessages;

    public PromptData(List<Map<String, String>> systemMessages, List<Map<String, String>> recentMessages) {
        this.systemMessages = systemMessages;
        this.recentMessages = recentMessages;
    }

    public List<Map<String, String>> getSystemMessages() {
        return systemMessages;
    }

    public List<Map<String, String>> getRecentMessages() {
        return recentMessages;
    }
}
