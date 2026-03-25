package sele906.dev.beluo_backend.user.repository.user;

import sele906.dev.beluo_backend.user.domain.User;

public interface UserRepositoryCustom {
    User userOverview(String userId);
    User userDetail(String userId);
    void updateById(String userId, User user);
    void anonymizeById(String userId);
    void updateAiModel(String userId, String aiModel);
}
