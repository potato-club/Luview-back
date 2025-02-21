package solo.project.service.Impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.dto.mail.password.request.PasswordResetConfirmDto;
import solo.project.dto.mail.password.request.PasswordResetRequestDto;
import solo.project.dto.mail.password.response.PasswordResponseDto;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.ForbiddenException;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.TokenCreationException;
import solo.project.repository.UserRepository;
import solo.project.service.MailService;
import solo.project.service.redis.RedisEmailAuthentication;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MailServiceImpl implements MailService {

    @Qualifier("gmail")
    private final JavaMailSender gmailSender;
    private final RedisEmailAuthentication redisEmailAuthentication;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String gmailUsername;

    public static String generateAuthCode() {
        StringBuilder secretKey = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            secretKey.append(random.nextInt(10)); // 6자리로 설정
        }
        return secretKey.toString();
    }

    @Override
    public void sendEmail(String toEmail) {
        // 기존 OTP 데이터 삭제
        redisEmailAuthentication.deleteExistingOtp(toEmail);
        MimeMessage mimeMessage = gmailSender.createMimeMessage();
        String emailKey = generateAuthCode();

        try {
            composeEmailMessage(toEmail, mimeMessage, emailKey);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("메일 전송 중 오류 발생", e);
            throw new RuntimeException("메일 전송 실패", e);
        }

        // OTP 데이터 Redis에 저장 (5분 유효)
        redisEmailAuthentication.setEmailOtpDataExpire(emailKey, toEmail, 60 * 5);
        gmailSender.send(mimeMessage);
    }

    //회원가입용
    @Override
    public void verifyEmail(String key, HttpServletResponse response) {
        Object otpData = redisEmailAuthentication.getEmailOtpData(key);
        if (otpData == null) {
            throw new NotFoundException("인증 코드가 만료되었거나 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }
        // OTP에 저장된 이메일 가져오기
        String email = otpData.toString();

        // OTP 검증 후 사용한 OTP는 삭제
        redisEmailAuthentication.deleteEmailOtpData(key);
        redisEmailAuthentication.setEmailVerified(email, true, 60 * 5);

        response.setHeader("Email-Verified", "true");
    }

    @Transactional
    @Override
    public String requestPasswordReset(PasswordResetRequestDto requestDTO) throws Exception {
        String email = requestDTO.getEmail();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new NotFoundException("해당 이메일로 등록된 사용자가 없습니다.", ErrorCode.NOT_FOUND_EMAIL_EXCEPTION);
        }
        String resetCode=generateAuthCode();
        // 5분 유효
        redisEmailAuthentication.setPasswordResetToken(email, resetCode, 5 * 60);

        // 이메일 본문 구성 (비밀번호 재설정 링크)
        String content = "<p>비밀번호 재설정 요청이 접수되었습니다.</p>"
                + "<p>아래 코드를 입력하여 비밀번호를 재설정해 주세요.</p>"
                + "<p style=\"font-size:24px; font-weight:bold;\">" + resetCode + "</p>"
                + "<p>코드 유효기간: 5분</p>";

        PasswordResponseDto emailDTO = PasswordResponseDto.builder()
                .email(email)
                .title("비밀번호 재설정 코드")
                .text(content)
                .build();

        sendPasswordResetEmail(emailDTO);

        return "비밀번호 재설정 이메일을 전송했습니다.";
    }

    @Override
    @Transactional
    public String resetPassword(PasswordResetConfirmDto confirmDTO) {
        String inputCode = confirmDTO.getCode();
        String newPassword = confirmDTO.getNewPassword();

        // Redis에 저장된 토큰과 비교
        String email = redisEmailAuthentication.getPasswordResetToken(inputCode);
        if (email==null) {
            throw new TokenCreationException("유효하지 않거나 만료된 토큰입니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        // 사용자 조회 및 비밀번호 업데이트
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.",ErrorCode.NOT_FOUND_EMAIL_EXCEPTION));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 토큰 삭제 (재사용 방지)
        redisEmailAuthentication.deletePasswordResetToken(inputCode);
        return "비밀번호가 성공적으로 변경되었습니다.";
    }


    private void composeEmailMessage(String toEmail, MimeMessage mimeMessage, String secretKey)
            throws MessagingException, UnsupportedEncodingException {

        mimeMessage.addRecipients(MimeMessage.RecipientType.TO, toEmail);
        mimeMessage.setSubject("Luview 인증 코드");

        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("<html>")
                .append("<body style=\"margin:0; padding:0; background-color:#f4f4f4; font-family: Arial, sans-serif;\">")
                .append("<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" ")
                .append("style=\"border-collapse: collapse; margin:50px auto; background-color:#ffffff; border-radius:8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);\">")
                .append("<tr>")
                .append("    <td align=\"center\" style=\"padding: 40px 0 30px 0;\">")
                .append("        <h1 style=\"color: #333333; margin: 0;\">이메일 인증 요청</h1>")
                .append("    </td>")
                .append("</tr>")
                .append("<tr>")
                .append("    <td style=\"padding: 20px 30px 40px 30px;\">")
                .append("        <p style=\"font-size: 16px; color: #555555; text-align: center; margin: 0;\">")
                .append("            회원가입을 완료하시려면 아래의 인증 코드를 입력해 주세요.")
                .append("        </p>")
                .append("    </td>")
                .append("</tr>")
                .append("<tr>")
                .append("    <td align=\"center\" style=\"padding: 20px;\">")
                .append("        <span style=\"display: inline-block; font-size: 32px; font-weight: bold; color: #2a9d8f; ")
                .append("                     padding: 10px 20px; border: 2px dashed #2a9d8f; border-radius: 8px;\">")
                .append(secretKey)
                .append("        </span>")
                .append("    </td>")
                .append("</tr>")
                .append("</table>")
                .append("</body>")
                .append("</html>");


        mimeMessage.setText(msgBuilder.toString(), "utf-8", "html");

        mimeMessage.setFrom(new InternetAddress(gmailUsername, "Luview"));
    }

    private void sendPasswordResetEmail(PasswordResponseDto emailDto) {
        MimeMessage mimeMessage = gmailSender.createMimeMessage();
        try {
            mimeMessage.addRecipients(MimeMessage.RecipientType.TO, emailDto.getEmail());
            mimeMessage.setSubject(emailDto.getTitle());
            mimeMessage.setText(emailDto.getText(), "utf-8", "html");
            mimeMessage.setFrom(new InternetAddress(gmailUsername, "Luview"));
            gmailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new ForbiddenException("이메일 전송을 실패했습니다." ,ErrorCode.UNAUTHORIZED_EXCEPTION);
        }
    }
}