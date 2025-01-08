package solo.project.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "couple")
public class Couple extends BaseTimeEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)  // Couple 1 ~ 1 User
  @JoinColumn(name = "user1_id")
  private User user1;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user2_id")
  private User user2;

  public Couple(User user1, User user2) {
    this.user1 = user1;
    this.user2 = user2;
  }
}
