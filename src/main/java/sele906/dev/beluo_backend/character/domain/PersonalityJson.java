package sele906.dev.beluo_backend.character.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalityJson {

    private Appearance appearance;
    private String background;
    private String occupation;
    private Personality personality;
    private SpeechStyle speechStyle;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Appearance {
        private String height;
        private String build;
        private String hair;
        private String expression;
        private String style;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Personality {
        private String type;
        private List<String> traits;
        private int emotionalOpenness; // 1~10
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpeechStyle {
        private String tone;
        private String formality;
        private List<String> quirks;
        private List<String> sentenceEndings; // 예: ["~까?", "~하지?", "~잖아"]
    }
}
