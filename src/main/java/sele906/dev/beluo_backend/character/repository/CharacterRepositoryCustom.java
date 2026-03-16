package sele906.dev.beluo_backend.character.repository;

import sele906.dev.beluo_backend.character.domain.Character;

import java.util.List;

public interface CharacterRepositoryCustom {
    List<Character> requestRecentCharacters(List<String> blockedIds);

    List<Character> requestPopularCharacters(List<String> blockedIds);

    List<Character> requestLikedCharacters(String userId);

    void increaseConvCount(String characterId);

    //좋아요

    void increaseLikeCount(String characterId);

    void decreaseLikeCount(String characterId);
}
