package solo.project.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommentRequestDto {
  @Schema(description = "댓글 내용")
  String content;
}
