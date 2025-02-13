package solo.project.repository.review;

import solo.project.dto.review.response.MainReviewResponseDto;
import solo.project.dto.review.response.ReviewResponseDto;
import solo.project.entity.Review;

import java.util.List;

public interface ReviewRepositoryCustom {
    ReviewResponseDto getReviewDetail(Long reviewId);
    List<Review> findPopularReview();
    List<Review> findPopularByLikes();
    List<MainReviewResponseDto> searchReview(String keyword);

}
