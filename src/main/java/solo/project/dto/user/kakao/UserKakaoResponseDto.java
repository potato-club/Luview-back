package solo.project.dto.user.kakao;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserKakaoResponseDto {

    @Schema(description = "Email")
    private String email;

    @Schema(description = "응답 코드", example = "200_OK / 201_Created")
    private String responseCode;
}
