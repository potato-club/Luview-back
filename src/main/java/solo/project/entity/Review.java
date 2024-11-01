package solo.project.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="reviews")
public class Review extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @Column(columnDefinition = "TINYINT(1)")
  private boolean deleted;

  @Builder
  public Review(Long id, String title, String content, boolean deleted) {
    this.id = id;
    this.title = title;
    this.content = content;
    this.deleted = deleted;
  }

  public void updateReview() {  // dto 만들어서 매개변수로 받을 예정
    this.title = title;
    this.content = content;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
