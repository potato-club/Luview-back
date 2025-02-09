package solo.project.repository.review;

import solo.project.dto.Review.response.ReviewResponseDto;
import solo.project.entity.Review;

import java.util.List;

public interface ReviewRepositoryCustom {
    ReviewResponseDto getReviewDetail(Long reviewId);
    List<Review> findPopularReview();

}
