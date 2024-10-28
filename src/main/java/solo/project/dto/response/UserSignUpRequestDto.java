package solo.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import solo.project.entity.User;
import solo.project.enums.LoginType;
import solo.project.enums.UserRole;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignUpRequestDto {
    @Schema(description = "Email")
    private String email;

    @Schema(description = "일반 로그인 패스워드", example = "null or password")
    private String password;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "USER / MANAGER", example = "USER / MANAGER")
    private UserRole userRole;

    @Schema(description = "NORMAL / KAKAO", example = "NORMAL / KAKAO")
    private LoginType loginType;

    public User toEntity() {
        return User.builder()
                .uid(String.valueOf(UUID.randomUUID()))
                .email(email)
                .password(password)
                .nickname(nickname)
                .userRole(userRole)
                .loginType(loginType)
                .build();
    }
}

