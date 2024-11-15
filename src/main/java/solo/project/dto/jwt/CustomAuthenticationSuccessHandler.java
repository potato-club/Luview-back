package solo.project.dto.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 로그인 성공 후 텍스트 메시지만 출력
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);  // 성공 상태 코드

        String message = "성공하셨습니다!";
        response.getWriter().write(message); // 텍스트 메시지를 응답으로 반환
        response.getWriter().flush();
    }
}
