package solo.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import solo.project.enums.UserRole;

@Builder
@Data
@AllArgsConstructor
public class UserUpdateRequestDto {
    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "유저 역할")
    private UserRole userRole;

}
