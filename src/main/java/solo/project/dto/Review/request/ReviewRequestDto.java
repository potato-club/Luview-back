package solo.project.dto.Review.request;

import io.swagger.v3.oas.annotations.media.Schema;
import solo.project.dto.Place.request.PlaceRequestDto;

import java.util.List;

public class ReviewRequestDto {
  @Schema(description = "제목")
  private String title;
  @Schema(description = "내용")
  private String content;
  @Schema(description = "장소")
  private List<PlaceRequestDto> places;
}
