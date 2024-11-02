package solo.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "likes")
public class Like {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(columnDefinition = "TINYINT(1)")
  private boolean isLiked;

  @ManyToOne(fetch = FetchType.LAZY)  // Like n ~ 1 User
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)  // Like n ~ 1 Review
  @JoinColumn(name = "review_id")
  private Review review;


}
