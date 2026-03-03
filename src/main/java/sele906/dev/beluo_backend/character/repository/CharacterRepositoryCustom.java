package sele906.dev.beluo_backend.character.repository;

import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.chat.domain.Conversation;

import java.util.List;

public interface CharacterRepositoryCustom {
    List<Character> requestRecentCharacters();
}
