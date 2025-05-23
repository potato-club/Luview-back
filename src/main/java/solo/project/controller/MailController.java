package solo.project.controller;

import io.jsonwebtoken.security.Password;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.mail.EmailCheckDto;
import solo.project.dto.mail.EmailRequestDto;
import solo.project.dto.mail.password.request.PasswordResetConfirmDto;
import solo.project.dto.mail.password.request.PasswordResetRequestDto;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.service.MailService;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
@Tag(name="Email Controller" ,description = "이메일 인증 API")
@Slf4j
public class MailController {
    private final MailService mailService;

    @Operation(summary = "Email 인증번호 발송")
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(String ToEmail) {
        mailService.sendEmail(ToEmail);
        return ResponseEntity.ok("입력하신 이메일로 인증번호가 전달되었습니다.");
    }

    @Operation(summary = "인증 코드 확인")
    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(String key, HttpServletResponse response) {
        mailService.verifyEmail(key, response);
        return ResponseEntity.ok("2차 인증이 정상적으로 처리되었습니다.");
    }

    @Operation(summary = "비밀번호 재설정 요청")
    @PostMapping("/password_reset")
    public ResponseEntity<String> requestPasswordReset(@RequestBody PasswordResetRequestDto requestDto) throws Exception {
        String passwordReset=mailService.requestPasswordReset(requestDto);
        return ResponseEntity.ok(passwordReset);
    }

    @Operation(summary = "비밀번호 재설정 확인")
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestBody PasswordResetConfirmDto requestDto) throws Exception {
        String confirm = mailService.resetPassword(requestDto);
        return ResponseEntity.ok(confirm);
    }
}
