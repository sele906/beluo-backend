package sele906.dev.beluo_backend.user.repository.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import sele906.dev.beluo_backend.user.domain.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);
}
