package solo.project.service;


import com.sun.tools.javac.Main;
import jakarta.servlet.http.HttpServletRequest;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
  //리뷰 생성
  void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request);
  //리뷰 전체 조회 - 리뷰글 페이지 뷰
  List<MainReviewResponseDto> getMainReviews(Pageable pageable);
  //리뷰 카테고리 별 조회 - 리뷰글 페이지 뷰(카레고리 정렬)
  List<ReviewResponseDto> getReviewsByCategory(Pageable pageable);
  //리뷰 개별 조회 - 리뷰글 상세페이지 뷰
  ReviewResponseDto getReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request);
  //리뷰 사용자 별 조회 - 마이페이지 리뷰글 뷰
  List<ReviewResponseDto> getUserReviews(HttpServletRequest request);
  //리뷰 수정
  void updateReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request);
  //리뷰 삭제
  void deleteReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request);


}
