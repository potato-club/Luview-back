package solo.project.dto.User.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequestDto {
    @Schema(description = "Email")
    private String email;

    @Schema(description = "일반 로그인 패스워드")
    private String password;

}
