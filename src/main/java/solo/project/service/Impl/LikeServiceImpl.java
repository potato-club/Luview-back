package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.entity.Like;
import solo.project.entity.Review;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.LikeOperationException;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.LikeRepository;
import solo.project.repository.review.ReviewRepository;
import solo.project.service.LikeService;
import solo.project.service.UserService;
import solo.project.service.redis.RedisLikeService;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

  private final UserService userService;
  private final ReviewRepository reviewRepository;
  private final LikeRepository likeRepository;
  private final RedisLikeService redisLikeService;

  @Override
  public void addLike(Long reviewId, HttpServletRequest request) {
    User user = authenticateUser(request);
    Review review = fetchReview(reviewId);

    // 이미 좋아요를 누른 상태라면 예외 발생
    if (likeRepository.findByUserAndReview(user, review) != null) {
      throw new LikeOperationException("이미 좋아요를 누른 상태입니다.",ErrorCode.ALREADY_CHECKED_LIKE_EXCEPTION);
    }

    Like like = Like.builder()
            .user(user)
            .review(review)
            .build();
    likeRepository.save(like);
    redisLikeService.incrementLikeCount(review.getId());
  }

  @Override
  public void removeLike(Long reviewId, HttpServletRequest request) {
    User user = authenticateUser(request);
    Review review = fetchReview(reviewId);
    Like existingLike = likeRepository.findByUserAndReview(user, review);

    if (existingLike == null) {
      throw new LikeOperationException("좋아요를 누르지 않은 상태입니다.",ErrorCode.NOT_FOUND_LIKE_EXCEPTION);
    }

    likeRepository.delete(existingLike);
    redisLikeService.decrementLikeCount(review.getId());
  }

  private User authenticateUser(HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if (user == null) {
      throw new UnAuthorizedException("로그인 후 이용 가능합니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }
    return user;
  }

  private Review fetchReview(Long reviewId) {
    return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("찾을 수 없는 리뷰글입니다.", ErrorCode.NOT_FOUND_EXCEPTION));
  }
}
