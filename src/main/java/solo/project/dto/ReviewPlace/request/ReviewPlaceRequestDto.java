package solo.project.dto.ReviewPlace.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPlaceRequestDto {
  @Schema(description = "별점")
  private int rating;
}
