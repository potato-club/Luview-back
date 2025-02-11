package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import org.springframework.data.domain.Pageable;
import solo.project.dto.file.FileRequestDto;

import java.io.IOException;
import java.util.List;

public interface ReviewService {
  void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request, List<MultipartFile> files) throws IOException;
  List<MainReviewResponseDto> getMainReviews(Pageable pageable);
  List<MainReviewResponseDto> getReviewsByCategory(String category, Pageable pageable);
  ReviewResponseDto getReviewDetail(Long reviewId);
  void updateReview(Long id, ReviewRequestDto reviewRequestDto, HttpServletRequest request, List<MultipartFile> newFiles, List<FileRequestDto> deleteFiles) throws IOException;
  void deleteReview(Long id, HttpServletRequest request);
  void incrementViewCount(Long reviewId);
  int getViewCount(Long reviewId);
  List<MainReviewResponseDto> getPopularReviews(HttpServletRequest request);
  List<MainReviewResponseDto> getPopularReviewsByLikes(HttpServletRequest request);
  List<MainReviewResponseDto> searchReviews(HttpServletRequest request,String keyword);
}
