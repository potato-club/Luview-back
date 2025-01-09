package solo.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "favorites")
public class Favorites {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)  // Favorites n ~ 1 User
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)  // Favorites n ~ 1 Review
  @JoinColumn(name = "review_id")
  private Review review;

  public Favorites(User user, Review review) {
    this.user = user;
    this.review = review;
  }

}
