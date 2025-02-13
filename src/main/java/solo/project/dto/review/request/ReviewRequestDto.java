package solo.project.dto.review.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import solo.project.dto.place.request.PlaceRequestDto;

import java.util.List;

@Data
public class ReviewRequestDto {
  @Schema(description = "제목")
  private String title;
  @Schema(description = "내용")
  private String content;
  @Schema(description = "장소")
  private List<PlaceRequestDto> places;
}
