package solo.project.dto.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import solo.project.error.ErrorJwtCode;
import solo.project.service.RedisJwtService;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 처리 서비스
    private final RedisJwtService redisJwtService; // Redis에서 토큰 정보 처리 서비스

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI(); // 요청 URI를 가져옵니다.

        // Swagger API 관련 경로는 필터를 거치지 않도록 설정
        if (path.contains("/swagger") || path.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response); // 필터 체인 계속 진행
            return;
        }

        // 로그인, 회원가입, 이메일 관련 경로는 인증 체크 없이 지나가도록 설정
        if (path.contains("/users/login") || path.contains("/users/signup") || path.contains("/users/mail")) {
            filterChain.doFilter(request, response); // 필터 체인 계속 진행
            return;
        }

        // 요청에서 Access Token과 Refresh Token을 추출
        String accessToken = jwtTokenProvider.resolveAccessToken(request);
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        ErrorJwtCode errorCode;

        try {
            // Access Token이 없고 Refresh Token이 있는 경우 (토큰 재발급 요청일 때)
            if (accessToken == null && refreshToken != null) {
                // Refresh Token이 유효한지, Redis에서 확인
                if (jwtTokenProvider.validateRefreshToken(refreshToken) && redisJwtService.isRefreshTokenValid(refreshToken)
                        && path.contains("/reissue")) {
                    filterChain.doFilter(request, response); // 필터 체인 계속 진행 (재발급 요청인 경우)
                }
            }
            // Access Token이 아예 없는 경우, 인증 없이 지나감
            else if (accessToken == null) {
                filterChain.doFilter(request, response);
                return;
            }
            // Access Token이 있는 경우, 유효한지 확인하고 인증을 설정
            else {
                // 토큰이 유효한지 검증하고, 유효하면 인증을 설정
                if (jwtTokenProvider.validateAccessToken(accessToken)) {
                    this.setAuthentication(accessToken); // 유효한 토큰이면 인증을 설정
                }
            }
        } catch (MalformedJwtException e) { // 잘못된 JWT 토큰 처리
            errorCode = ErrorJwtCode.INVALID_JWT_TOKEN; // 에러 코드 설정
            setResponse(response, errorCode); // 에러 응답 반환
            return;
        } catch (ExpiredJwtException e) { // 만료된 JWT 토큰 처리
            errorCode = ErrorJwtCode.JWT_TOKEN_EXPIRED;
            setResponse(response, errorCode);
            return;
        } catch (UnsupportedJwtException e) { // 지원하지 않는 JWT 토큰 처리
            errorCode = ErrorJwtCode.UNSUPPORTED_JWT_TOKEN;
            setResponse(response, errorCode);
            return;
        } catch (IllegalArgumentException e) { // JWT 토큰에 claims가 없거나 잘못된 형식 처리
            errorCode = ErrorJwtCode.EMPTY_JWT_CLAIMS;
            setResponse(response, errorCode);
            return;
        } catch (SignatureException e) { // JWT 서명 불일치 처리
            errorCode = ErrorJwtCode.JWT_SIGNATURE_MISMATCH;
            setResponse(response, errorCode);
            return;
        } catch (RuntimeException e) { // 그 외의 예외 처리
            errorCode = ErrorJwtCode.JWT_COMPLEX_ERROR;
            setResponse(response, errorCode);
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        filterChain.doFilter(request, response); // 필터 체인 계속 진행
    }

    // 에러 응답을 클라이언트에 반환하는 메서드
    private void setResponse(HttpServletResponse response, ErrorJwtCode errorCode) throws IOException {
        JSONObject json = new JSONObject();
        response.setContentType("application/json;charset=UTF-8"); // 응답 타입 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 상태 코드 401 (Unauthorized) 설정

        // 에러 코드와 메시지를 JSON으로 응답
        json.put("code", errorCode.getCode());
        json.put("message", errorCode.getMessage());

        response.getWriter().print(json); // 응답 출력
        response.getWriter().flush(); // 응답 플러시
    }

    // 토큰으로부터 사용자 인증 정보를 추출하고 SecurityContext에 설정하는 메서드
    private void setAuthentication(String token) throws Exception {
        Authentication authentication = jwtTokenProvider.getAuthentication(token); // 토큰으로부터 인증 정보 추출
        SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 정보를 SecurityContext에 저장
    }
}
