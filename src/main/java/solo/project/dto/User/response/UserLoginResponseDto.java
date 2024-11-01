package solo.project.dto.User.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginResponseDto {
    @Schema(description = "응답 코드", example = "성공했습니다!! 200 ")
    private String responseCode;
}
