package solo.project.service;

import jakarta.mail.MessagingException;
import solo.project.dto.mail.EmailDto;

import java.io.UnsupportedEncodingException;

public interface MailService {
    String sendMail(EmailDto emailDto)throws MessagingException, UnsupportedEncodingException;
}
