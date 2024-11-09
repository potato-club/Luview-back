package solo.project.dto.Review.response;

import java.time.LocalDateTime;

public class ReviewResponseDto {
  private String title;
  private String content;
  private int viewCount;
  private int likeCount;

  private LocalDateTime createdAt;
}
