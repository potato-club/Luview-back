package solo.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "files")
public class File {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long fileId;

  @Column(nullable = false, length = 512)
  private String fileUrl;

  @Column(nullable = false)
  private String fileName;

  private boolean isThumbnail; //썸네일 여부 (리뷰 썸네일)

  @ManyToOne(fetch = FetchType.LAZY)  //한명의 사람은 하나의 프로필
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)  // File n ~ 1 Review
  @JoinColumn(name = "review_id")
  private Review review;

  @Builder
  public File(String fileUrl, String fileName) {
    this.fileUrl = fileUrl;
    this.fileName = fileName;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public void setReview(Review review) {
    this.review = review;
  }
}
