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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.repository.UserRepository;
import solo.project.service.MailService;
import solo.project.service.UserService;
import solo.project.service.redis.RedisEmailAuthentication;

import java.io.UnsupportedEncodingException;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MailServiceImpl implements MailService {

    @Qualifier("gmail")
    private final JavaMailSender gmailSender;
    private final RedisEmailAuthentication redisEmailAuthentication;
    private final UserRepository userRepository;
    private final UserService userService;

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


    private void composeEmailMessage(String toEmail, MimeMessage mimeMessage, String secretKey)
            throws MessagingException, UnsupportedEncodingException {

        mimeMessage.addRecipients(MimeMessage.RecipientType.TO, toEmail);
        mimeMessage.setSubject("Luview 인증 코드");

        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("<html>")
                .append("<body>")
                .append("<h1>이메일 인증 요청</h1>")
                .append("<p>회원가입을 완료하시려면 아래의 인증 코드를 입력해 주세요.</p>")
                .append("<p style=\"font-size: 24px; font-weight: bold;\">")
                .append(secretKey)
                .append("</p>")
                .append("</body>")
                .append("</html>");


        mimeMessage.setText(msgBuilder.toString(), "utf-8", "html");

        mimeMessage.setFrom(new InternetAddress(gmailUsername,"Luview"));
    }
}
