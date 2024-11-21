package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.entity.Like;
import solo.project.entity.Review;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.repository.LikeRepository;
import solo.project.repository.ReviewRepository;
import solo.project.service.LikeService;
import solo.project.service.UserService;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
  private final UserService userService;
  private final ReviewRepository reviewRepository;
  private final LikeRepository likeRepository;

  @Override
  public String likeReview(Long review_id, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if (user == null)
      throw new NotFoundException("로그인 후 이용 가능합니다.",ErrorCode.NOT_FOUND_EXCEPTION);

    Review review = reviewRepository.findById(review_id).orElse(null);
    if (review == null)
      throw new NotFoundException("찾을 수 없는 리뷰글입니다.", ErrorCode.NOT_FOUND_EXCEPTION);

    Like like = likeRepository.findByUserAndReview(user,review);
    if(like == null){
      like = Like.builder()
          .user(user)
          .review(review)
          .build();
      likeRepository.save(like);
      review.upReviewLikeCount();
      return "해당 리뷰글에 좋아요가 추가되었습니다.";
    }

    likeRepository.delete(like);
    review.upReviewLikeCount();
    return "해당 리뷰글에 좋아요가 취소되었습니다.";
  }
}
