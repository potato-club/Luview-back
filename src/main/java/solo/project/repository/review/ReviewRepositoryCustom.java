package solo.project.repository.review;

import solo.project.dto.Review.response.ReviewResponseDto;

public interface ReviewRepositoryCustom {
    ReviewResponseDto getReviewDetail(Long reviewId);

}
