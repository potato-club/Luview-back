package solo.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

  @Column(nullable = false)
  private String phone_number;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;


}

