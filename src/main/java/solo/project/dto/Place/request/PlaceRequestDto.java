package solo.project.dto.Place.request;

import lombok.Getter;

@Getter
public class PlaceRequestDto {
  private String kakaoPlaceId;
  private String addressName;
  private String categoryGroupName;
  private String placeName;
  private String phoneNumber;
  private Double latitude;
  private Double longitude;
  private int rating;
}
