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
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.ReviewPlaceRepository;
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
  private final ReviewPlaceRepository reviewPlaceRepository;

  @Override
  public void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request) {
    // 사용자 인증 및 식별 request에서 토큰 값을 받아서 유저 찾기
    User user = userService.findUserByToken(request);
    if(user == null)
      throw new UnAuthorizedException("로그인 후 리뷰글 작성 가능", ErrorCode.UNAUTHORIZED_EXCEPTION);

    // 장소를 한개 이상 받기 위한 검사 코드 (장소가 없거나 비어있는 리스트일 경우 에러)
    if(reviewRequestDto.getPlaces() == null || reviewRequestDto.getPlaces().isEmpty())
      throw new NotFoundException("장소를 등록해주세요!", ErrorCode.NOT_FOUND_EXCEPTION);

    // 리뷰 엔티티 생성 (한 번만 생성)
    Review review = Review.builder()
        .user(user)
        .title(reviewRequestDto.getTitle())
        .content(reviewRequestDto.getContent())
        .viewCount(0)
        .likeCount(0)
        .commentCount(0)
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
  public List<MainReviewResponseDto> getReviewByFavorites(Pageable pageable) {
    return List.of();
  }

  @Override
  public List<ReviewResponseDto> getUserReviews(HttpServletRequest request) {
    return List.of();
  }

  @Override
  public void updateReview(Long id, ReviewRequestDto reviewRequestDto, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if(user == null)
      throw new UnAuthorizedException("리뷰글 수정 권한이 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
    Review review = reviewRepository.findById(id).orElse(null);
    if (review == null)
      throw new NotFoundException("수정할 수 없는 리뷰글입니다", ErrorCode.NOT_FOUND_EXCEPTION);
    if (review.getUser() != user)
      throw new UnAuthorizedException("리뷰글 수정 권한이 없습니다", ErrorCode.UNAUTHORIZED_EXCEPTION);

    review.update(reviewRequestDto);  // 제목, 내용 수정

    List<ReviewPlace> oldPlace = reviewPlaceRepository.findByReview(review); // 기존 리뷰장소(장소와 별점)
    List<PlaceRequestDto> newPlace = reviewRequestDto.getPlaces();  // 새로 받은 장소

    // 기존 장소와 새로 받은 장소의 변경 여부를 판단
    if(hasChanges(oldPlace, newPlace)){
      // 기존 리뷰-장소 관계 삭제
      reviewPlaceService.deleteReviewPlaces(review);
      // 새로운 장소 저장 및 리뷰-장소 관계 생성
      List<Place> places = placeService.createPlace(newPlace);
      reviewPlaceService.createReviewPlaces(review, places, newPlace);
    }
  }

  @Override
  public void deleteReview(Long id, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if(user == null)
      throw new UnAuthorizedException("리뷰글 삭제 권한이 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);

    Review review = reviewRepository.findById(id).orElse(null);
    if(review == null)
      throw new NotFoundException("삭제할 리뷰글이 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    if(review.getUser() != user)
      throw new UnAuthorizedException("리뷰글 삭제 권한이 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);

    reviewRepository.delete(review);
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
      throw new IllegalArgumentException("해당 카테고리 또는 리뷰에 대한 장소를 찾을 수 없습니다.");
    }

    return MainReviewResponseDto.builder()
                                .id(review.getId())
                                .category(place.getCategory())
                                .placeName(place.getPlaceName())
                                .title(review.getTitle())
                                .likeCount(review.getLikeCount())
                                .commentCount(review.getCommentCount())
                                .userNickName(review.getUser().getNickname())
                                .firstFileUrl("review.getFiles().get(0).getFileUrl()")  // file 구현후 수정해야함
                                .build();
  }

  // 기존 장소와 새로 입력된 장소의 변경 여부를 판단
  private boolean hasChanges(List<ReviewPlace> oldPlace, List<PlaceRequestDto> newPlaces) {
    // 리뷰장소에서 장소 가져오기
    List<Place> existingPlaces = oldPlace.stream().map(ReviewPlace::getPlace).toList();
    List<Integer> ratings = oldPlace.stream().map(ReviewPlace::getRating).toList();
    // 1. 장소 개수 비교
    if (existingPlaces.size() != newPlaces.size()) {
      return true;
    }

    // 2. 각 장소 순서 비교
    for (int i = 0; i < existingPlaces.size(); i++) {
      Place existingPlace = existingPlaces.get(i);
      PlaceRequestDto newPlace = newPlaces.get(i);

      // 장소의 고유 식별자(ID) 순서 변경
      if (!existingPlace.getKakaoPlaceId().equals(newPlace.getKakaoPlaceId()) ||
          !ratings.get(i).equals(newPlace.getRating())) {
        return true;
      }
    }
    return false;
  }
}
