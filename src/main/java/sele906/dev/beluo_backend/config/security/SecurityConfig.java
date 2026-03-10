package sele906.dev.beluo_backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Autowired
//    private CustomOAuth2UserService customOAuth2UserService;
//
//    @Autowired
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @Autowired
//    private OAuth2SuccessHandler oAuth2SuccessHandler;
//
//    @Autowired
//    private OAuth2FailureHandler oAuth2FailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))  // 세션 미사용
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
//                        .anyRequest().authenticated()                              // 나머지는 인증 필요
//                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
//                .oauth2Login(oauth2 -> oauth2
//                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
//                        .successHandler(oAuth2SuccessHandler)
//                        .failureHandler(oAuth2FailureHandler)
//                )
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}