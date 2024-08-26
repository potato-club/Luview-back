package Couplace.service.impl;

import Couplace.jwt.JwtUtil;
import Couplace.repository.TokenResponse;
import Couplace.service.TokenService;
import Couplace.token.RefreshToken;
import Couplace.token.RefreshTokenRepository;
import Couplace.token.TokenErrorResult;
import Couplace.token.TokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    @Value("${jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME; // 액세스 토큰 유효기간

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    public TokenResponse reissueAccessToken(String authorizationHeader) {
        String refreshToken = jwtUtil.getTokenFromHeader(authorizationHeader);
        if (refreshToken == null) {
            throw new TokenException(TokenErrorResult.INVALID_REFRESH_TOKEN);
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new TokenException(TokenErrorResult.INVALID_REFRESH_TOKEN);
        }

        RefreshToken existRefreshToken = refreshTokenRepository.findByUserId(UUID.fromString(userId));
        if (existRefreshToken == null || !existRefreshToken.getToken().equals(refreshToken) || jwtUtil.isTokenExpired(refreshToken)) {
            throw new TokenException(TokenErrorResult.INVALID_REFRESH_TOKEN);
        }

        // 액세스 토큰 재발급
        String accessToken = jwtUtil.generateAccessToken(UUID.fromString(userId), ACCESS_TOKEN_EXPIRATION_TIME);

        return TokenResponse.builder()
                .access_token(accessToken)
                .build();
    }
}
