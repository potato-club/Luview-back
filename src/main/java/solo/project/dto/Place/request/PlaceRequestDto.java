package solo.project.dto.Place.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class PlaceRequestDto {
  @Schema(description = "카카오ID")
  private String kakaoPlaceId;
  @Schema(description = "주소")
  private String addressName;
  @Schema(description = "카테고리")
  private String categoryGroupName;
  @Schema(description = "상호명")
  private String placeName;
  @Schema(description = "전화번호")
  private String phoneNumber;
  @Schema(description = "위도")
  private Double latitude;
  @Schema(description = "경도")
  private Double longitude;
  @Schema(description = "별점")
  private int rating;
}
