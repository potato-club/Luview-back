package solo.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPlace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private int rating;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id",nullable = false)
  private Review review;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id",nullable = false)
  private Place place;

  public ReviewPlace(Review review, Place place, int rating) {
    this.review = review;
    this.place = place;
    this.rating = rating;
  }

  public void updatePlace(Place place, int rating) {
    this.place = place;
    this.rating = rating;
  }

}
