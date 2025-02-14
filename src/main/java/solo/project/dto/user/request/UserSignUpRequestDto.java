package solo.project.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import solo.project.entity.User;
import solo.project.enums.LoginType;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignUpRequestDto {
    @Schema(description = "Email")
    private String email;

    @Schema(description = "일반 로그인 패스워드", example = "null or password")
    private String password;

    @Schema(description = "이름")
    private String name;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "생년월일")
    private LocalDate birthDate;

    @Schema(description = "NORMAL / KAKAO", example = "NORMAL / KAKAO")
    private LoginType loginType;

    public User toEntity() {
        return User.builder()
                .email(email)
                .name(name)
                .password(password)
                .nickname(nickname)
                .birthDate(birthDate)
                .loginType(loginType)
                .build();
    }
}

