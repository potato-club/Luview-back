package solo.project.dto.review.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//게시물 리스트들 쫙~~~~~
public class MainReviewResponseDto {
  @Schema(description = "리뷰ID")
  private Long reviewId;
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
  private String nickName; // 작성자
  @Schema(description = "조회수")
  private int viewCount; // 조회수 필드 추가
  @Schema(description = "썸네일")
  private String thumbnailUrl; //대표 이미지
}
