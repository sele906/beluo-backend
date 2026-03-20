package sele906.dev.beluo_backend.character.repository;

import sele906.dev.beluo_backend.character.domain.Character;

import java.util.List;

public interface CharacterRepositoryCustom {
    List<Character> findRecentCharacters(List<String> blockedIds);

    List<Character> findPopularCharacters(List<String> blockedIds);

    List<Character> findLikedCharacters(List<String> characterIds);

    void increaseConvCount(String characterId);

    //좋아요

    void increaseLikeCount(String characterId);

    void decreaseLikeCount(String characterId);

    //마이페이지

    List<Character> findRecentCreatedCharacters(String userId);

    List<Character> findCreatedCharacters(String userId);

    List<Character> findBlockedCharacters(List<String> characterIds);

    void updateByIdAndUserId(String id, String userId, Character character);

    void softDeleteByIdAndUserId(String id, String userId);

    void softDeleteByUserId(String userId);

    List<Character> searchCharacters(String keyword, List<String> blockedIds);
}
