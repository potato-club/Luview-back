package solo.project.dto.Review.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import solo.project.dto.Place.request.PlaceRequestDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
  @Schema(description = "제목")
  private String title;
  @Schema(description = "내용")
  private String content;
  @Schema(description = "장소")
  private List<PlaceRequestDto> places;
}
