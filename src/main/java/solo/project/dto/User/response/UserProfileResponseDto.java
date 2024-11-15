package solo.project.dto.User.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import solo.project.enums.LoginType;
import solo.project.enums.UserRole;

@Getter
public class UserProfileResponseDto {
    @Schema(description = "닉네임")
    private final String nickname;

    @Schema(description = "로그인 타입", example = "NORMAL / KAKAO")
    private final LoginType loginType;

    @Schema(description = "유저 역할", example = "USER / MANAGER")
    private final UserRole userRole;

    @Schema(description = "사진 이름")
    private final String fileName;

    @Schema(description = "사진 URL")
    private final String fileUrl;


    public UserProfileResponseDto(String nickname, LoginType loginType, UserRole userRole,  String fileName, String fileUrl) {
        this.nickname = nickname;
        this.loginType = loginType;
        this.userRole = userRole;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
}
