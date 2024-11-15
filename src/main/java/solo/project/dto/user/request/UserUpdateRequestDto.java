package solo.project.dto.User.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import solo.project.enums.UserRole;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class UserUpdateRequestDto {
    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "유저 역할")
    private UserRole userRole;

    @Schema(description = "신규 프로필 사진")
    private List<MultipartFile> file;

}
