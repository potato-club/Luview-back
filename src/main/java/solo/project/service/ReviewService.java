package solo.project.service;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import solo.project.entity.Review;

import java.net.http.HttpResponse;

public interface ReviewService {
  //리뷰 생성
  public void createReview(HttpRequest request, HttpServletRequest response);
  //리뷰 전체 조회
  //리뷰 개별 조회
  //리뷰 수정
  //리뷰 삭제

}
