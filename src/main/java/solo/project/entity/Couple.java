package solo.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "couple")
public class Couple extends BaseTimeEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(columnDefinition = "TINYINT(1)")
  private boolean status;

  @OneToOne(fetch = FetchType.LAZY)  // Couple 1 ~ 1 User
  @JoinColumn(name = "user1_id")
  private User user1;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user2_id")
  private User user2;

}
