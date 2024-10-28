package solo.project.dto.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import solo.project.enums.UserRole;
import solo.project.error.ErrorCode;
import solo.project.error.exception.TokenCreationException;
import solo.project.repository.UserRepository;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final UserRepository userRepository;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.accessExpiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenValidTime;

    //검증키 생성
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //액세스 토큰 생성 ,, 전체적인 에러 말고 토큰 에러를 따로 만든다면 가독성이 좋아질 것 같음
    public String createAccessToken(Long id, UserRole role) {
        try {
            return this.createToken(id, role, accessTokenValidTime, "access");
        } catch (Exception e) {
            throw new TokenCreationException("액세스 토큰 생성 실패",ErrorCode.ACCESS_TOKEN_CREATION_FAILED)
        }
    }

    //리프레쉬 토큰 생성 ,, 이것도 따로 예외를 만들어서 나중에 적용
    public String createRefreshToken(Long id, UserRole role) {
        try {
            return this.createToken(id, role, refreshTokenValidTime, "refresh");
        } catch (Exception e) {
            throw new TokenCreationException("리프레쉬 토큰 생성 실패",ErrorCode.REFRESH_TOKEN_CREATION_FAILED);
        }
    }

    public String createToken(Long id, UserRole role, long tokenValid, String tokenType) throws Exception {
    }
}

