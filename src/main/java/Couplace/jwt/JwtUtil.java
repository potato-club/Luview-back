package Couplace.jwt;

import Couplace.token.TokenErrorResult;
import Couplace.token.TokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 액세스 토큰을 발급하는 메서드
    public String generateAccessToken(UUID userId, long expirationMillis) {
        log.info("액세스 토큰이 발행되었습니다.");

        return Jwts.builder()
                .claim("userId", userId.toString()) // 클레임에 userId 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(this.getSigningKey()) // Signing key 설정
                .compact();
    }

    // 리프레쉬 토큰을 발급하는 메서드
    public String generateRefreshToken(UUID userId, long expirationMillis) {
        log.info("리프레쉬 토큰이 발행되었습니다.");

        return Jwts.builder()
                .claim("userId", userId.toString()) // 클레임에 userId 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(this.getSigningKey()) // Signing key 설정
                .compact();
    }

    // 응답 헤더에서 액세스 토큰을 반환하는 메서드
    public String getTokenFromHeader(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

    // 토큰에서 유저 id를 반환하는 메서드
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder() // parserBuilder()를 사용하여 파서 생성
                    .setSigningKey(this.getSigningKey()) // 서명 검증 키 설정
                    .build() // JwtParser 빌드
                    .parseClaimsJws(token) // JWT 파싱 및 서명 검증
                    .getBody(); // 클레임(Claims) 추출

            log.info("유저 id를 반환합니다.");
            return claims.get("userId", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 유효하지 않은 경우
            log.warn("유효하지 않은 토큰입니다.");
            throw new TokenException(TokenErrorResult.INVALID_TOKEN);
        }
    }

    // Jwt 토큰의 유효기간을 확인하는 메서드
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = Jwts.parserBuilder() // parserBuilder()를 사용하여 파서 생성
                    .setSigningKey(this.getSigningKey()) // 서명 검증 키 설정
                    .build() // JwtParser 빌드
                    .parseClaimsJws(token) // JWT 파싱 및 서명 검증
                    .getBody() // 클레임(Claims) 추출
                    .getExpiration(); // 유효기간 확인

            log.info("토큰의 유효기간을 확인합니다.");
            return expirationDate.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 유효하지 않은 경우
            log.warn("유효하지 않은 토큰입니다.");
            throw new TokenException(TokenErrorResult.INVALID_TOKEN);
        }
    }
}
