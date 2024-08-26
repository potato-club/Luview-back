package Couplace.security;

import Couplace.handler.OAuthLoginFailureHandler;
import Couplace.handler.OAuthLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserDetailsService userService;
    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final OAuthLoginFailureHandler oAuthLoginFailureHandler;

    @Value("${security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    // Naver
    @Value("${security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setAllowedMethods(Collections.singletonList("*"));
            config.setAllowedOriginPatterns(Collections.singletonList("*"));
            config.setAllowCredentials(true);
            return config;
        };
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
                kakaoClientRegistration(),
                googleClientRegistration(),
                naverClientRegistration()
        );
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/h2-console/**")
                .requestMatchers(new AntPathRequestMatcher("/static/**"));
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();
    }

    private ClientRegistration kakaoClientRegistration() {
        return ClientRegistration.withRegistrationId("kakao")
                .clientId(kakaoClientId)
                .clientSecret(kakaoClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
                .scope("account_email")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .clientName("Kakao")
                .build();
    }

    private ClientRegistration naverClientRegistration() {
        return ClientRegistration.withRegistrationId("naver")
                .clientId(naverClientId)
                .clientSecret(naverClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/naver")
                .scope("name")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .userNameAttributeName("response")
                .clientName("Naver")
                .build();
    }

    // 시큐리티 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/**").permitAll()
                )
                .oauth2Login(oauth ->
                        oauth
                                .successHandler(oAuthLoginSuccessHandler) // 로그인 성공 시 핸들러
                                .failureHandler(oAuthLoginFailureHandler) // 로그인 실패 시 핸들러
                                .clientRegistrationRepository(clientRegistrationRepository()) // 클라이언트 등록 추가
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/signup"),
                                new AntPathRequestMatcher("/user")
                        ).permitAll()
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/articles")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                );

        return httpSecurity.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
