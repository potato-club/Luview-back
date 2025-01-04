package solo.project.dto.User.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import solo.project.enums.LoginType;
import solo.project.enums.UserRole;

@Getter
@Builder
public class UserProfileResponseDto {
  @Schema(description = "닉네임")
  private final String nickname;

  @Schema(description = "로그인 타입", example = "NORMAL / KAKAO")
  private final LoginType loginType;

  public UserProfileResponseDto(String nickname, LoginType loginType) {
    this.nickname = nickname;
    this.loginType = loginType;
  }
}