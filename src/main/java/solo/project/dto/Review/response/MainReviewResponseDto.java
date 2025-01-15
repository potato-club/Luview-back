package solo.project.dto.Review.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import solo.project.entity.Place;
import solo.project.entity.Review;
import solo.project.entity.ReviewPlace;
import solo.project.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainReviewResponseDto {
  @Schema(description = "카테고리")
  private String category; // 첫 번째 장소의 카테고리
  @Schema(description = "상호명")
  private String placeName; // 첫 번째 장소의 상호명
  @Schema(description = "제목")
  private String title; // 리뷰 제목
  @Schema(description = "좋아요수 ")
  private int likeCount; // 좋아요 수
  @Schema(description = "댓글 수")
  private int commentCount; // 댓글 수
  @Schema(description = "작성자")
  private String NickName; // 작성자
  @Schema(description = "썸네일")
  private String thumbnailUrl; //대표 이미지
}
