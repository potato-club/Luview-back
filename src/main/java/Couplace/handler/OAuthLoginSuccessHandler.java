package Couplace.handler;

import Couplace.entity.User;
import Couplace.info.KakaoUserInfo;
import Couplace.info.NaverUserInfo;
import Couplace.info.OAuth2UserInfo;
import Couplace.repository.UserRepository;
import Couplace.info.GoogleUserInfo;
import Couplace.jwt.JwtUtil;
import Couplace.token.RefreshToken;
import Couplace.token.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${jwt.redirect}")
    private String REDIRECT_URI;

    @Value("${jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        final String provider = token.getAuthorizedClientRegistrationId();
        OAuth2UserInfo oAuth2UserInfo;

        switch (provider) {
            case "google" -> {
                log.info("구글 로그인 요청");
                oAuth2UserInfo = new GoogleUserInfo(token.getPrincipal().getAttributes());
            }
            case "kakao" -> {
                log.info("카카오 로그인 요청");
                oAuth2UserInfo = new KakaoUserInfo(token.getPrincipal().getAttributes());
            }
            case "naver" -> {
                log.info("네이버 로그인 요청");
                oAuth2UserInfo = new NaverUserInfo(token.getPrincipal().getAttributes());
            }
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        if (oAuth2UserInfo == null) {
            throw new IllegalArgumentException("OAuth2UserInfo is null");
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String name = oAuth2UserInfo.getName();

        log.info("Extracted Name: {}", name);
        log.info("Extracted Provider ID (Name): {}", providerId);

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is missing from the OAuth2 response");
        }

        User existUser = userRepository.findByProviderId(providerId);
        User user;

        if (existUser == null) {
            log.info("신규 유저입니다. 등록을 진행합니다.");
            user = User.builder()
                    .userId(UUID.randomUUID())
                    .name(name)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(user);
        } else {
            log.info("기존 유저입니다.");
            refreshTokenRepository.deleteByUserId(existUser.getUserId());
            user = existUser;
        }

        log.info("유저 이름 : {}", name);
        log.info("PROVIDER : {}", provider);
        log.info("PROVIDER_ID (Name) : {}", providerId);

        // 리프레쉬 토큰 발급 후 저장
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), REFRESH_TOKEN_EXPIRATION_TIME);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId(user.getUserId())
                .token(refreshToken)
                .refreshToken(UUID.randomUUID().toString())  // 추가된 필드에 대한 설정
                .build();
        refreshTokenRepository.save(newRefreshToken);

        // 액세스 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), ACCESS_TOKEN_EXPIRATION_TIME);

        // 이름, 액세스 토큰, 리프레쉬 토큰을 담아 리다이렉트
        String encodedName = URLEncoder.encode(name, "UTF-8");
        String redirectUri = String.format(REDIRECT_URI, encodedName, accessToken, refreshToken);
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
