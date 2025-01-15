package solo.project.entity;

import jakarta.persistence.*;
import lombok.*;
import solo.project.dto.Review.request.ReviewRequestDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="reviews")
public class Review extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "review_id")
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @Column(columnDefinition = "TINYINT(1)")
  private boolean deleted;

  @Column(nullable = false)
  private int viewCount;

  @Column(nullable = false)
  private int likeCount;

  @Column(nullable = false)
  private int commentCount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 아래 필드는 리뷰에 대한 모든 "좋아요, 장소, 사진, 댓글"들을 저장하는 리스트입니다.
  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReviewPlace> reviewPlaces = new ArrayList<>();

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Like> likes = new ArrayList<>();

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<File> files = new HashSet<>();

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Comment> comments = new HashSet<>();

  @Builder
  public Review(Long id, String title, String content, boolean deleted) {
    this.id= id;
    this.title = title;
    this.content = content;
    this.deleted = deleted;
  }


  public void update(ReviewRequestDto reviewRequestDto) {
    this.title = reviewRequestDto.getTitle();
    this.content = reviewRequestDto.getContent();
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public void upReviewLikeCount() {
    this.likeCount += 1;
  }

  public void downReviewLikeCount() {
    this.likeCount -= 1;
  }

  public void upViewCount() {
    this.viewCount += 1;
  }

  public void upCommentCount() {
    this.commentCount += 1;
  }

  public void downCommentCount() {
    this.commentCount -= 1;
  }

  public void downCommentCount(int childCommentCount){
    this.commentCount -= 1+childCommentCount;
  }


  public void setComments(Set<Comment> comments) {
    this.comments = comments;
  }

  public void setFiles(Set<File> files) {
    this.files = files;
  }

  public void addFile(File file) {
    files.add(file);
    file.setReview(this);
  }
}
