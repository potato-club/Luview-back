package solo.project.service;

import jakarta.servlet.http.HttpServletResponse;
import solo.project.dto.mail.password.request.PasswordResetConfirmDto;
import solo.project.dto.mail.password.request.PasswordResetRequestDto;

public interface MailService {
    void sendEmail(String toEmail);
    void verifyEmail(String key, HttpServletResponse response);
    String requestPasswordReset(PasswordResetRequestDto requestDTO) throws Exception;
    String resetPassword(PasswordResetConfirmDto confirmDTO);
}
