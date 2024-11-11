package solo.project.dto.Review.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
  @Schema(description = "제목")
  private String title;
  @Schema(description = "내용")
  private String content;
  @Schema(description = "조회수")
  private int viewCount;
  @Schema(description = "좋아요수")
  private int likeCount;
  @Schema(description = "작성일")
  private LocalDateTime createdAt;
}
