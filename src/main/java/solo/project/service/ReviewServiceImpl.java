package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import solo.project.entity.Place;
import solo.project.entity.Review;
import solo.project.entity.ReviewPlace;
import solo.project.entity.User;
import solo.project.repository.ReviewPlaceRepository;
import solo.project.repository.ReviewRepository;
import solo.project.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
  private final ReviewPlaceRepository reviewPlaceRepository;
  private final UserService userService;
  private final ReviewRepository reviewRepository;
  private final PlaceService placeService;


  @Override
  public void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request) {
    // 사용자 인증 및 식별 request에서 토큰 값을 받아서 유저 찾기
    User user = userService.findUserByToken(request);

    // 리뷰 엔티티 생성 (한 번만 생성)
    Review review = Review.builder()
        .user(user)
        .title(reviewRequestDto.getTitle())
        .content(reviewRequestDto.getContent())
        .build();

    // 리뷰를 저장하고, 리뷰 ID를 받아옴
    Review savedReview = reviewRepository.save(review);

    // 장소와 리뷰 연결 및 평점 설정
    List<ReviewPlace> reviewPlaces = new ArrayList<>();

    for (PlaceRequestDto placeDto : reviewRequestDto.getPlaces()) {
      // 장소 생성 또는 조회
      Place place = placeService.createPlace(placeDto);

      // ReviewPlace 엔티티 생성
      ReviewPlace reviewPlace = new ReviewPlace(placeDto.getRating(), savedReview, place);

      // ReviewPlace 리스트에 추가
      reviewPlaces.add(reviewPlace);
    }

    // ReviewPlace 객체들을 한 번에 저장
    reviewPlaceRepository.saveAll(reviewPlaces);
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
