package solo.project.dto.Review.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class ReviewRequestDto {
  @Schema(description = "제목")
  private String title;
  @Schema(description = "내용")
  private String content;
}
