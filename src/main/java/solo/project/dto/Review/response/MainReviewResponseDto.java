package solo.project.dto.Review.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainReviewResponseDto {
  @Schema(description = "리뷰글 리스트")
  private List<ReviewResponseDto> reviews;
}
