package solo.project.dto.User.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import solo.project.enums.UserRole;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class UserUpdateRequestDto {
  @Schema(description = "닉네임")
  private String nickname;

  @Schema(description = "생년월일")
  private LocalDate birthday;

  @Schema(description = "신규 프로필 사진")
  private List<MultipartFile> file;

  @Schema(description = "기존 프로필 사진 이름-변경시에만")
  private String fileName;

  @Schema(description = "기존 프로필 사진 URL-변경시에만")
  private String fileUrl;
}
