package solo.project.service.Impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private final JavaMailSender gmailSender;
    private final RedisEmailAuthentication redisEmailAuthentication;
    private int authNumber;
    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String gmailUsername;

    @Autowired
    public MailServiceImpl(@Qualifier("gmail") JavaMailSender gmailSender,
                            RedisEmailAuthentication redisEmailAuthentication,
                            UserRepository userRepository,
                            UserServiceImpl userService) {
        this.gmailSender = gmailSender;
        this.redisEmailAuthentication= redisEmailAuthentication;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public static String generateAuthCode() {
        StringBuilder secretKey = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 6; i++){
            secretKey.append(random.nextInt(10)); //6자리로 설정
        }
        return secretKey.toString();
    }

    public String sendSignUpEmail(String email) {
        generateAuthCode();
        String fromEmail = "jihu65487@gmail.com"; // 발신자 이메일 (설정 파일에 등록된 이메일)
        String title = "회원 가입 인증 이메일 입니다.";
        String content = "Luview 이메일 인증번호입니다. 감사합니다." +
                "<br><br>" +
                "인증 번호는 " + authNumber + "입니다." +
                "<br>" +
                "인증번호를 제대로 입력해주세요";

        return Integer.toString(authNumber);
    }

    public void sandEmail(String fromEmail) {
        redisEmailAuthentication.deleteExistData(fromEmail);
        MimeMessage mimeMessage = gmailSender.createMimeMessage();

        String emailKey=generateAuthCode();
        composeEmailMessage(fromEmail,mimeMessage,"gmail",emailKey);

        redisEmailAuthentication.setDataExpire(emailKey,fromEmail,60*5);

    }

    public boolean checkAuthNum(String email, String authNum) {
        log.info("입력 인증번호: {}", authNum);
        log.info("입력 이메일: {}", email);
        Object redisData = redisEmailAuthentication.getData(authNum);
        return redisData != null && redisData.equals(email);
    }

    private void composeEmailMessage(String fromEmail,MimeMessage mimeMessage,String secretKey) throws MessagingException, UnsupportedEncodingException {
        mimeMessage.addRecipients(MimeMessage.RecipientType.TO, fromEmail);
        mimeMessage.setSubject("Luview 인증 코드");

        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333;\">")
                .append("<h1 style=\"font-size: 32px; text-align: center; padding: 20px 30px 10px; margin: 0;\">이메일 인증 요청</h1>")
                .append("<p style=\"font-size: 18px; text-align: center; margin: 0 30px 30px;\">회원가입을 완료하시려면 아래의 인증 코드를 입력해 주세요.</p>")
                .append("<div style=\"display: flex; justify-content: center; margin: 20px 30px;\">")
                .append("<table style=\"border-collapse: collapse; background-color: #F4F4F4; border-radius: 8px;\">")
                .append("<tr>")
                .append("<td style=\"padding: 20px 40px; font-size: 36px; font-weight: bold; text-align: center;\">")
                .append(secretKey)
                .append("</td>")
                .append("</tr>")
                .append("</table>")
                .append("</div>")
                .append("</div>");

        String msg = msgBuilder.toString();
        mimeMessage.setText(msg,"utf-8","html");

    }
}
