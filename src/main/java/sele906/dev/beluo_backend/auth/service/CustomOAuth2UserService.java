package sele906.dev.beluo_backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import sele906.dev.beluo_backend.auth.domain.User;
import sele906.dev.beluo_backend.auth.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        //스프링이 구글에서 받아온 사용자 정보
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        System.out.println("attributes: " + attributes);

        //DB에서 찾거나 신규가입
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setProvider("google");
                    newUser.setProviderId(googleId);
                    newUser.setRole("USER");
                    newUser.setCreatedAt(Instant.now());
                    return userRepository.save(newUser);
                });

        System.out.println("user: " + user);

        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())), attributes, "sub");
    }
}
