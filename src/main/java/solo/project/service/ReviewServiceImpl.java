package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  @Override
  public void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request) {

  }

  @Override
  public List<MainReviewResponseDto> getMainReviews(ReviewRequestDto reviewRequestDto, HttpServletRequest request) {
    return List.of();
  }

  @Override
  public List<ReviewResponseDto> getReviewsByCategory(ReviewRequestDto reviewRequestDto, String category) {
    return List.of();
  }

  @Override
  public ReviewResponseDto getReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request) {
    return null;
  }

  @Override
  public List<ReviewResponseDto> getUserReviews(HttpServletRequest request) {
    return List.of();
  }

  @Override
  public void updateReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request) {

  }

  @Override
  public void deleteReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request) {

  }
}
