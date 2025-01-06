package solo.project.dto.kakao.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserKakaoResponseDto {

    @Schema(description = "Id")
    private Long id;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "응답 코드", example = "200_OK / 201_Created")
    private String responseCode;
}
