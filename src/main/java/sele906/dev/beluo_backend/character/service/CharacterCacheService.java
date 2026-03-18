package sele906.dev.beluo_backend.character.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.character.domain.Character;
import sele906.dev.beluo_backend.character.repository.CharacterRepository;

import java.util.List;

@Service
public class CharacterCacheService {

    @Autowired
    private CharacterRepository characterRepository;

    @Cacheable("recentCharacters")
    public List<Character> getRecentCharacters() {
        return characterRepository.requestRecentCharacters(List.of());
    }

    @Cacheable("popularCharacters")
    public List<Character> getPopularCharacters() {
        return characterRepository.requestPopularCharacters(List.of());
    }

    @CacheEvict(value = {"recentCharacters", "popularCharacters"}, allEntries = true)
    @Scheduled(fixedDelay = 300000) // 5분마다 캐시 갱신
    public void evictCache() {}
}
