package solo.project.service;


import jakarta.servlet.http.HttpServletRequest;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import org.springframework.data.domain.Pageable;
import solo.project.entity.Review;

import java.util.List;

public interface ReviewService {
  //리뷰 생성
  void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request);
  //리뷰 전체 조회 - 리뷰글 페이지 뷰(최신글 정렬)
  List<MainReviewResponseDto> getMainReviews(Pageable pageable);
  //리뷰 카테고리 별 조회 - 리뷰글 페이지 뷰(카레고리 정렬)
  List<MainReviewResponseDto> getReviewsByCategory(String Category, Pageable pageable);
  //리뷰 개별 조회 - 리뷰글 상세페이지 뷰
  Review getReview(Long id);
  //리뷰 즐겨찾기 조회
  List<MainReviewResponseDto> getReviewByFavorites(Pageable pageable);
  //리뷰 사용자 별 조회 - 마이페이지 리뷰글 뷰
  List<ReviewResponseDto> getUserReviews(HttpServletRequest request);
  //리뷰 수정
  void updateReview(Long id, ReviewRequestDto reviewRequestDto, HttpServletRequest request);
  //리뷰 삭제
  void deleteReview(Long id, HttpServletRequest request);


}
