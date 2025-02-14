package solo.project.dto.review.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import solo.project.dto.comment.CommentResponseDto;
import solo.project.dto.reviewPlace.response.ReviewPlaceResponseDto;
import solo.project.dto.file.FileResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {

  @Schema(description = "리뷰 id")
  private Long reviewId;

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

  @Schema(description = "작성자 닉네임")
  private String nickname;

  @Schema(description = "댓글 수")
  private int commentCount;

  @Schema(description = "방문한 장소들")
  private List<ReviewPlaceResponseDto> reviewPlaces;  // 필요한 필드만 DTO로 변환

  @Schema(description = "첨부된 이미지 정보")
  private List<FileResponseDto> files;        // 여러 이미지 목록

  @Schema(description = "대표 이미지(썸네일)")
  private String thumbnailUrl;               // 첫 번째 파일의 URL을 썸네일로

  @Schema(description = "댓글 목록")
  private List<CommentResponseDto> comments; // 댓글 목록
}

