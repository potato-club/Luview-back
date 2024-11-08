package solo.project.service;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import solo.project.entity.Review;

import java.net.http.HttpResponse;
import java.util.List;

public interface ReviewService {
  //리뷰 생성
  void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request);
  //리뷰 전체 조회 - 리뷰글 페이지 뷰
  List<MainReviewResponseDto> getMainReviews(ReviewRequestDto reviewRequestDto, HttpServletRequest request);
  //리뷰 카테고리 별 조회 - 리뷰글 페이지 뷰(카레고리 정렬)
  List<ReviewResponseDto> getReviewsByCategory(ReviewRequestDto reviewRequestDto, String category);
  //리뷰 개별 조회 - 리뷰글 상세페이지 뷰
  ReviewResponseDto getReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request);
  //리뷰 사용자 별 조회 - 마이페이지 리뷰글 뷰
  List<ReviewResponseDto> getUserReviews(HttpServletRequest request);
  //리뷰 수정
  void updateReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request);
  //리뷰 삭제
  void deleteReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request);

}
