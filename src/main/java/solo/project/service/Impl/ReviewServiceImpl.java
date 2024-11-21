package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import solo.project.entity.Place;
import solo.project.entity.Review;
import solo.project.entity.ReviewPlace;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.TokenCreationException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.ReviewRepository;
import org.springframework.data.domain.Pageable;
import solo.project.service.PlaceService;
import solo.project.service.ReviewPlaceService;
import solo.project.service.ReviewService;
import solo.project.service.UserService;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
  private final UserService userService;
  private final ReviewRepository reviewRepository;
  private final PlaceService placeService;
  private final ReviewPlaceService reviewPlaceService;


  @Override
  public void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request) {
    // 사용자 인증 및 식별 request에서 토큰 값을 받아서 유저 찾기
    User user = userService.findUserByToken(request);
    // 장소를 한개 이상 받기 위한 검사 코드 (장소가 없거나 비어있는 리스트일 경우 에러)
    if(reviewRequestDto.getPlaces() == null || reviewRequestDto.getPlaces().isEmpty())
      throw new TokenCreationException("장소를 등록해주세요!", ErrorCode.REVIEW_PLACE_NULL);

    // 리뷰 엔티티 생성 (한 번만 생성)
    Review review = Review.builder()
        .user(user)
        .title(reviewRequestDto.getTitle())
        .content(reviewRequestDto.getContent())
        .build();
    // 리뷰를 저장하고, 리뷰 ID를 받아옴
    Review savedReview = reviewRepository.save(review);

    //장소 저장 후 그 장소들 반환
    List<Place> places = placeService.createPlace(reviewRequestDto.getPlaces());

    // 장소랑 리뷰 받아서 리뷰랑 장소 중간 테이블 생성
    reviewPlaceService.createReviewPlaces(savedReview, places, reviewRequestDto.getPlaces());
  }

  @Override
  public List<MainReviewResponseDto> getMainReviews(Pageable pageable) {
    Page<Review> reviews = reviewRepository.findAllByOrderByCreatedDateDesc(pageable); // 최신순 정렬
    String category = "";
    return reviews.stream()
        .map(review -> createMainReviewDto(review, category))
        .collect(Collectors.toList());
  }

  @Override
  public List<MainReviewResponseDto> getReviewsByCategory(String category, Pageable pageable) {
    Page<Review> reviews = reviewRepository.findByPlaceCategory(category, pageable);
    return reviews.stream()
        .map(review -> createMainReviewDto(review, category))
        .collect(Collectors.toList());
  }

  @Override
  public Review getReview(Long id) {
    return null;
  }

  @Override
  public List<ReviewResponseDto> getUserReviews(HttpServletRequest request) {
    return List.of();
  }

  @Override
  public void updateReview(Long id, ReviewRequestDto reviewRequestDto, HttpServletRequest request) {
    User user = userService.findUserByToken(request);

    Review review = reviewRepository.findById(id).orElse(null);
    if (review == null)
      throw new NotFoundException("수정할 수 없는 리뷰글입니다", ErrorCode.NOT_FOUND_EXCEPTION);
    if (review.getUser() != user)
      throw new UnAuthorizedException("게시글 수정 권한이 없습니다", ErrorCode.UNAUTHORIZED_EXCEPTION);

    review.update(reviewRequestDto);  // 제목, 내용 수정

    for(PlaceRequestDto placeRequestDto : reviewRequestDto.getPlaces()) {

    }
  }

  @Override
  public void deleteReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request) {

  }

  private MainReviewResponseDto createMainReviewDto(Review review, String category) {
    Place place = null;
    if(category == null || category.isEmpty()){
      place = review.getReviewPlaces().get(0).getPlace();
    } else {
      for (ReviewPlace reviewPlace : review.getReviewPlaces()) {
        if (reviewPlace.getPlace().getCategory().equals(category)) {
          place = reviewPlace.getPlace();
        }
      }
    }

    if(place == null) {
      throw new IllegalArgumentException("No Place found for the given category or review.");
    }

    return MainReviewResponseDto.builder()
                                .id(review.getId())
                                .category(place.getCategory())
                                .placeName(place.getPlaceName())
                                .title(review.getTitle())
                                .likeCount(0) // like 증감 코드 구현후 수정 해야함
                                .commentCount(0)  // comment 코드 구현후 수정해야함
                                .userNickName(review.getUser().getNickname())
                                .firstFileUrl("review.getFiles().get(0).getFileUrl()")  // file 구현후 수정해야함
                                .build();
  }

}
