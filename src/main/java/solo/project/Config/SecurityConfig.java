package solo.project.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화
                .cors(Customizer.withDefaults()) // CORS 기본 설정
                .httpBasic(AbstractHttpConfigurer::disable) // 기본 HTTP 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable); // 폼 로그인 비활성화

        http.authorizeHttpRequests(authorize -> authorize
                // Swagger 관련 URL 허용
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui/index.html").permitAll()
                .anyRequest().permitAll() // 모든 요청에 대해 접근 허용
        );

        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // JWT 필터 제거
        // http.addFilterBefore(new JwtAuthorizationTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
