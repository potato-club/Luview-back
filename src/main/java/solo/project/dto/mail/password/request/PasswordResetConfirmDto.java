package solo.project.dto.mail.password.request;

import lombok.Data;

@Data
public class PasswordResetConfirmDto {
    private String code;
    private String newPassword;
}

