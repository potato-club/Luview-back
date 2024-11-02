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

  // 아래 필드는 리뷰에 대한 모든 "좋아요, 장소, 사진, 댓글"들을 저장하는 리스트입니다.
  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReviewPlace> reviewPlaces = new ArrayList<>();

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Like> likes = new ArrayList<>();

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<File> files = new ArrayList<>();

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

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
