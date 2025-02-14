package solo.project.dto.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class EmailCheckDto {
    @Email
    @NotEmpty(message = "이메일을 입력해주세요.")
    private String email;

    @NotEmpty(message = "인증번호를 입력해주세요.")
    private String authNum;
}
