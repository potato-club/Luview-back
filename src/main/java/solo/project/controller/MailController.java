package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.mail.EmailCheckDto;
import solo.project.dto.mail.EmailRequestDto;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.service.MailService;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
@Tag(name="Email Controller" ,description = "이메일 인증 API입니다.")
@Slf4j
public class MailController {
    private final MailService mailService;

    @Operation(summary = "Gmail 인증번호 발송")
    @PostMapping("/send")
    public ResponseEntity<String> sendSignupVerificationEmail(@RequestBody @Valid EmailRequestDto emailRequestDto) {
        mailService.sendSignUpEmail(emailRequestDto.getEmail());
        return ResponseEntity.ok("입력하신 이메일로 인증번호가 전달되었습니다.");
    }

    @Operation(summary = "Gmail 인증번호 확인")
    @PostMapping("/check")
    public ResponseEntity<String> verifyEmailAuth(@RequestBody @Valid EmailCheckDto emailCheckDto) {
        boolean isValid = mailService.checkAuthNum(emailCheckDto.getEmail(), emailCheckDto.getAuthNum());
        if (isValid) {
            return ResponseEntity.ok("Email이 인증 되었습니다.");
        } else {
            throw new NotFoundException("인증번호 또는 이메일이 올바르지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }
    }
}
