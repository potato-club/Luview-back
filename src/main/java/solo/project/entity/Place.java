package solo.project.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import solo.project.dto.place.request.PlaceRequestDto;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "places")
public class Place {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column(unique = true, nullable = false)
  private String kakaoPlaceId;

  @Column(nullable = false)
  private String addressName;

  @Column(nullable = false)
  private String category; // enum으로 바꾸기

  @Column(nullable = false)
  private String placeName;

  @Column
  private String phoneNumber;

  @Column
  private Double latitude;

  @Column
  private Double longitude;



  @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReviewPlace> reviewPlaces = new ArrayList<>();

  @Builder
  public Place(Long id, String kakaoPlaceId, String addressName, String category, String placeName, String phoneNumber, double latitude, double longitude) {
    this.id = id;
    this.kakaoPlaceId = kakaoPlaceId;
    this.addressName = addressName;
    this.category = category;
    this.placeName = placeName;
    this.phoneNumber = phoneNumber;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  static public Place toEntity(PlaceRequestDto placeRequestDto) {
    return Place.builder()
        .kakaoPlaceId(placeRequestDto.getKakaoPlaceId())
        .addressName(placeRequestDto.getAddressName())
        .category(placeRequestDto.getCategory())
        .placeName(placeRequestDto.getPlaceName())
        .phoneNumber(placeRequestDto.getPhoneNumber())
        .latitude(placeRequestDto.getLatitude())
        .longitude(placeRequestDto.getLongitude())
        .build();
  }
}

