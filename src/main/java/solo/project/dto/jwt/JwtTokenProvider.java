package solo.project.dto.jwt;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import solo.project.entity.User;
import solo.project.enums.UserRole;
import solo.project.error.ErrorCode;
import solo.project.error.exception.ExpiredRefreshTokenException;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.TokenCreationException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.UserRepository;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j

//액세스 리프레쉬 토큰 생성, 유효기간 만료, EMAIL값을 통해서 토큰 발급 처리
public class JwtTokenProvider {
    private final UserRepository userRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.accessExpiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenValidTime;

    @Value("${jwt.aesKey}")
    private String aesKey;

    //검증키 생성
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private void logError(Exception e, String message) {
        log.error(message, e); // 예외와 메시지를 로그에 기록
    }

    public String createAccessToken(String email, UserRole role) {
        try {
            return this.createToken(email, role, accessTokenValidTime, "access");
        } catch (Exception e) {
            logError(e, "액세스 토큰 생성 실패");
            throw new TokenCreationException("액세스 토큰 생성 실패", ErrorCode.ACCESS_TOKEN_CREATION_FAILED);
        }
    }

    public String createRefreshToken(String email, UserRole role) {
        try {
            return this.createToken(email, role, refreshTokenValidTime, "refresh");
        } catch (Exception e) {
            throw new TokenCreationException("리프레쉬 토큰 생성 실패", ErrorCode.REFRESH_TOKEN_CREATION_FAILED);
        }
    }

    public String createToken(String email, UserRole role, long tokenValid, String tokenType) throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pk", email);
        jsonObject.addProperty("role", role.ordinal());
        jsonObject.addProperty("tokenType", tokenType);

        Claims claims=Jwts.claims().subject(encrypt(jsonObject.toString())).build();
        Date date=new Date();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(date)
                .expiration(new Date(date.getTime()+tokenValid))
                .signWith(getSigningKey())
                .compact();
    }

    //소셜로그인 토큰 헤더 설정 액세스
    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader("authorization", "Bearer " + accessToken);
    }

    //토큰 헤더 설정
    public void setHeaderRefreshToken(HttpServletResponse response, String refreshToken) {
        response.setHeader("refreshToken", "Bearer " + refreshToken);
    }
    //AES를 사용하여 암호화
    private String encrypt(String plainToken) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec IV = new IvParameterSpec(aesKey.substring(0, 16).getBytes());

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, secretKeySpec, IV);

        byte[] encryptionByte = c.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));

        return Hex.encodeHexString(encryptionByte);
    }

    private String decrypt(String encodeText) throws Exception{
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8),"AES");
        IvParameterSpec IV  =new IvParameterSpec(aesKey.substring(0,16).getBytes());

        Cipher c= Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, secretKeySpec,IV);

        byte[] decodeByte=Hex.decodeHex(encodeText);

        return new String(c.doFinal(decodeByte),StandardCharsets.UTF_8);
    } //암호화 복호화 완벽하게 이해함

    public String extractValueFromToken(String token, String key) throws Exception {
        JsonElement value = extraValue(token).get(key);
        if (value == null) {
            throw new IllegalArgumentException("토큰에 키가 존재하지 않습니다: " + key);
        }
        return value.getAsString();
    }// 리팩토링

    public String extractEmail(String token) throws Exception {
        return extractValueFromToken(token, "pk");
    }

    public String extractRole(String token) throws Exception {
        return extractValueFromToken(token, "role").toString();
    }

    //토큰으로 역할 추출 , 우리는 유저 하나만 존재함 사용 x
    public String extractMemberId(String token) throws Exception {
        String email = extractEmail(token);
        String role = extractRole(token); // 토큰에서 역할 추출

        if ("0".equals(role)) {
            Optional<User> userOptional = userRepository.findByEmail(email); // 이메일로 사용자 찾기
            if (userOptional.isEmpty()) {
                throw new NotFoundException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
            }
            return userOptional.get().getEmail(); // 사용자 이메일 반환
        } else {
            throw new UnAuthorizedException("권한이 없는 사용자입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
    }

    public boolean isAdmin(String token) throws Exception {
        String role = extractRole(token);
        return role.equals("2");
    }

    private JsonObject extraValue(String token) throws Exception {
        String subject = extraAllClaims(token).getSubject();
        String decrypted=decrypt(subject);
        return new Gson().fromJson(decrypted, JsonObject.class);
    }

    private Claims extraAllClaims(String token) throws Exception {
        return getParser()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private JwtParser getParser(){
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build();
    }

    //토큰 유형 추출 RT 인지 AT인지
    public String extractTokenType(String token) throws Exception{
        JsonElement tokenType = extraValue(token).get("tokenType");
        return String.valueOf(tokenType);
    }

    //요청을 받으면 AT반환 없다면 null
    public String resolveAccessToken(HttpServletRequest request) {
        String accessToken = request.getHeader("authorization");
        String refreshToken = request.getHeader("refreshToken");
        if (accessToken != null && refreshToken == null) {
            return accessToken.substring(7);
        }
        return null;
    }


    //7개로 정의 RT재발급
    public String resolveRefreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("refreshToken");
        final int tokenPrefixLength = 7;
        if (refreshToken != null && refreshToken.length() > tokenPrefixLength) {
            return refreshToken.substring(tokenPrefixLength);
        }
        return null;
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) throws Exception{
        UserDetails userDetails= customUserDetailsService.loadUserByUsername(extractMemberId(token));
        return new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
    }

    //유효성 만료 액세스
    public boolean validateAccessToken(String accessToken) {
        try {
            Claims claims = extraAllClaims(accessToken);
            //만료 여부 확인
            return !claims.getExpiration().before(new Date());
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(null, null, "AccessToken is Expired");
        } catch (UnsupportedJwtException ex) {
            throw new UnsupportedJwtException("JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT claims string is empty");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //토근 유효성 + 만료 확인 리프레쉬
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = extraAllClaims(refreshToken);
            // 만료 여부 확인
            return !claims.getExpiration().before(new Date());
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("Invalid JWT token", e);
        } catch (ExpiredJwtException e) { //만료된 경우
            throw new ExpiredRefreshTokenException("1006", ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (UnsupportedJwtException ex) { // 지원 X인 JWT 일경우
            throw new UnsupportedJwtException("JWT token is unsupported", ex);
        } catch (IllegalArgumentException e) { //문자열이 빈 경우
            throw new IllegalArgumentException("JWT claims string is empty", e);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred during token validation", e);
        }
    }

    public String reissueAccessToken(String refreshToken, HttpServletResponse response) {
        try{
            this.validateRefreshToken(refreshToken);
            String email =findUserEmailByToken(refreshToken);
            Optional<User> user=userRepository.findByEmail(email);
            return createAccessToken(email, user.get().getUserRole());
        }catch (ExpiredJwtException e){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return ErrorCode.EXPIRED_ACCESS_TOKEN.getMessage();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    //RefreshToken은 AccessToken의 유효기간이 짧아 따로 구현하지 않음 그냥 리프레쉬는 새로 발급
    //하는 방식으로 함

    //리프레쉬 토큰을 받아서 이메일 찾음, 아니라면 예외를 던지고 이메일을 던짐
    public String findUserEmailByToken(String token)throws Exception{
        String accessTokenType= extractTokenType(token);

        if("access".equals(accessTokenType)){
            throw new UnAuthorizedException("AccessToken은 사용 할 수 없습니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        return token == null? null : userRepository.findByEmail(extractEmail(token))
                .orElseThrow(() -> new NotFoundException("토큰에 해당되는 사용자 이메일을 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION)).getEmail();
    }

    public Optional<User> extractEmailByRequest(HttpServletRequest request) throws Exception{
        String userToken= resolveAccessToken(request);
        String tokenId=extractEmail(userToken);
        return userRepository.findByEmail(tokenId);
    }
}

