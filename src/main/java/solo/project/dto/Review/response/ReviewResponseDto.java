package solo.project.dto.Review.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import solo.project.entity.ReviewPlace;
import solo.project.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
  @Schema(description = "id")
  private Long id;
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
  @Schema(description = "작성자")
  private String userName;
  @Schema(description = "리뷰글에 등록한 장소들")
  private List<ReviewPlace> reviewPlaces;

}
