package solo.project.dto.User.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import solo.project.enums.LoginType;

import java.time.LocalDate;

@Getter
public class UserProfileResponseDto {
  @Schema(description = "닉네임")
  private final String nickname;

  @Schema(description = "로그인 타입", example = "NORMAL / KAKAO")
  private final LoginType loginType;

  @Schema(description = "사진 이름")
  private final String fileName;

  @Schema(description = "생년월일")
  private LocalDate birthDate;

  @Schema(description = "사진 URL")
  private final String fileUrl;

  @Builder
  public UserProfileResponseDto(String nickname, LoginType loginType, String fileName,String fileUrl, LocalDate birthDate) {
    this.nickname = nickname;
    this.loginType = loginType;
    this.fileName = fileName;
    this.fileUrl = fileUrl;
    this.birthDate = birthDate;
  }

  //내 코드랑, 상대방이 연동이 되었는지, 생년월일도 보여주어야함 --
}