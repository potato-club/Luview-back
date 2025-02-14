package solo.project.service;

import jakarta.servlet.http.HttpServletResponse;

public interface MailService {
    void sendEmail(String toEmail);
    void verifyEmail(String key, HttpServletResponse response);
}
