package solo.project.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "places")
public class Place {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String kakaoplace_id;

  @Column(nullable = false)
  private String address_name;

  @Column(nullable = false)
  private String category_group_name;

  @Column(nullable = false)
  private String place_name;

  @Column
  private String phone_number;

  @Column
  private Double latitude;

  @Column
  private Double longitude;

  @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReviewPlace> reviewPlaces = new ArrayList<>();

  @Builder
  public Place(Long id, String kakaoplace_id, String address_name, String category_group_name, String place_name, String phone_number, double latitude, double longitude) {
    this.id = id;
    this.kakaoplace_id = kakaoplace_id;
    this.address_name = address_name;
    this.category_group_name = category_group_name;
    this.place_name = place_name;
    this.phone_number = phone_number;
    this.latitude = latitude;
    this.longitude = longitude;
  }

}

