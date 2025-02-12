package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import solo.project.dto.file.FileRequestDto;
import solo.project.entity.File;
import solo.project.entity.Place;
import solo.project.entity.Review;
import solo.project.entity.ReviewPlace;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.ReviewPlaceRepository;
import solo.project.repository.review.ReviewRepository;
import org.springframework.data.domain.Pageable;
import solo.project.service.ImageService;
import solo.project.service.PlaceService;
import solo.project.service.ReviewPlaceService;
import solo.project.service.ReviewService;
import solo.project.service.UserService;
import solo.project.service.redis.RedisReviewListService;
import solo.project.service.redis.RedisSearchService;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
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
  private final ImageService imageService;
  private static final String REVIEW_VIEW_COUNT_KEY_PREFIX = "review:view:";
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisSearchService redisSearchService;

  // 5분 지정
  private static final Duration POPULAR_REVIEWS_CACHE_DURATION = Duration.ofMinutes(5);
  private final RedisReviewListService redisReviewService;

  @Override
  public void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request, List<MultipartFile> files) throws IOException {
    User user = userService.findUserByToken(request);
    if (reviewRequestDto.getPlaces() == null || reviewRequestDto.getPlaces().isEmpty()) {
      throw new NotFoundException("장소를 등록해주세요!", ErrorCode.NOT_FOUND_EXCEPTION);
    }

    Review review = Review.builder()
            .user(user)
            .title(reviewRequestDto.getTitle())
            .content(reviewRequestDto.getContent())
            .viewCount(0)
            .likeCount(0)
            .commentCount(0)
            .build();
    Review savedReview = reviewRepository.save(review);

    // 장소 생성 + 리뷰-장소 연결
    List<Place> places = placeService.createPlace(reviewRequestDto.getPlaces());
    reviewPlaceService.createReviewPlaces(savedReview, places, reviewRequestDto.getPlaces());

    // 이미지 업로드
    if (files != null && !files.isEmpty()) {
      imageService.uploadImages(files, savedReview);
    }
  }

  @Override
  public List<MainReviewResponseDto> getMainReviews(Pageable pageable) {
    // 최신순 목록
    Page<Review> reviews = reviewRepository.findAllByOrderByCreatedDateDesc(pageable);
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
  @Transactional(readOnly = true)
  public ReviewResponseDto getReviewDetail(HttpServletRequest request,Long reviewId) {
    userService.findUserByToken(request);
    ReviewResponseDto reviewDetail = reviewRepository.getReviewDetail(reviewId);
    // Redis를 이용해 조회수 증가 (멀티쓰레드 방지)
    incrementViewCount(reviewId);

    int redisViewCount = getViewCount(reviewId);
    int totalViewCount = reviewDetail.getViewCount() + redisViewCount;
    reviewDetail.setViewCount(totalViewCount);

    return reviewDetail;
  }

  @Override
  public void updateReview(Long id, ReviewRequestDto reviewRequestDto, HttpServletRequest request, List<MultipartFile> newFiles, List<FileRequestDto> deleteFiles) throws IOException {
    User user = userService.findUserByToken(request);
    Review review = reviewRepository.findById(id).orElse(null);
    if (review == null) {
      throw new NotFoundException("수정할 수 없는 리뷰글입니다", ErrorCode.NOT_FOUND_EXCEPTION);
    }
    if (review.getUser() != user) {
      throw new UnAuthorizedException("리뷰글 수정 권한이 없습니다", ErrorCode.UNAUTHORIZED_EXCEPTION);
    }

    review.update(reviewRequestDto);
    List<ReviewPlace> oldPlace = reviewPlaceRepository.findByReview(review);
    List<PlaceRequestDto> newPlaces = reviewRequestDto.getPlaces();
    if (hasChanges(oldPlace, newPlaces)) {
      reviewPlaceService.deleteReviewPlaces(review);
      List<Place> places = placeService.createPlace(newPlaces);
      reviewPlaceService.createReviewPlaces(review, places, newPlaces);
    }

    // 이미지 업데이트
    imageService.updateImages(review, newFiles, deleteFiles);
  }

  @Override
  public void deleteReview(Long id, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    Review review = reviewRepository.findById(id).orElse(null);
    if (review == null) {
      throw new NotFoundException("삭제할 리뷰글이 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }
    if (review.getUser() != user) {
      throw new UnAuthorizedException("리뷰글 삭제 권한이 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
    }
    reviewRepository.delete(review);
  }

  // 메인 목록 DTO 변환용
  private MainReviewResponseDto createMainReviewDto(Review review, String category) {
    Place place = null;
    if (category == null || category.isEmpty()) {
      place = review.getReviewPlaces().get(0).getPlace();
    } else {
      for (ReviewPlace rp : review.getReviewPlaces()) {
        if (rp.getPlace().getCategory().equals(category)) {
          place = rp.getPlace();
          break;
        }
      }
    }
    if (place == null) {
      throw new IllegalArgumentException("해당 카테고리 또는 리뷰에 대한 장소를 찾을 수 없습니다.");
    }

    // 첫 번째 이미지를 썸네일로 사용
    List<File> fileList = new ArrayList<>(review.getFiles());
    String thumbUrl = fileList.isEmpty() ? null : fileList.get(0).getFileUrl();

    return MainReviewResponseDto.builder()
            .reviewId(review.getId())
            .category(place.getCategory())
            .placeName(place.getPlaceName())
            .title(review.getTitle())
            .likeCount(review.getLikeCount())
            .commentCount(review.getCommentCount())
            .nickName(review.getUser().getNickname())
            .thumbnailUrl(thumbUrl)
            .build();
  }

  // 장소 변경 여부 체크
  private boolean hasChanges(List<ReviewPlace> oldPlace, List<PlaceRequestDto> newPlaces) {
    List<Place> existingPlaces = oldPlace.stream().map(ReviewPlace::getPlace).toList();
    List<Integer> ratings = oldPlace.stream().map(ReviewPlace::getRating).toList();
    if (existingPlaces.size() != newPlaces.size()) {
      return true;
    }
    for (int i = 0; i < existingPlaces.size(); i++) {
      Place existingPlace = existingPlaces.get(i);
      PlaceRequestDto newPlace = newPlaces.get(i);
      if (!existingPlace.getKakaoPlaceId().equals(newPlace.getKakaoPlaceId()) ||
              !ratings.get(i).equals(newPlace.getRating())) {
        return true;
      }
    }
    return false;
  }

  // Redis를 이용한 리뷰 조회수 증가
  @Override
  public void incrementViewCount(Long reviewId) {
    String key = REVIEW_VIEW_COUNT_KEY_PREFIX + reviewId;
    ValueOperations<String, Object> ops = redisTemplate.opsForValue();
    ops.increment(key, 1);
  }

  // Redis에 저장된 조회수 가져오기
  @Override
  public int getViewCount(Long reviewId) {
    String key = REVIEW_VIEW_COUNT_KEY_PREFIX + reviewId;
    ValueOperations<String, Object> ops = redisTemplate.opsForValue();
    Object value = ops.get(key);
    if (value == null) {
      return 0;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    try {
      return Integer.parseInt(value.toString());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  // 인기 리뷰 (조회수 기준)
  @Override
  public List<MainReviewResponseDto> getPopularReviews(HttpServletRequest request) {
    userService.findUserByToken(request);
    Object cached = redisReviewService.getPopularReviews();
    if (cached instanceof List) {
      return (List<MainReviewResponseDto>) cached;
    }

    List<Review> reviews = reviewRepository.findPopularReview();
    List<MainReviewResponseDto> dtos = reviews.stream()
            .map(review -> {
              int redisViewCount = getViewCount(review.getId());
              int totalViewCount = review.getViewCount() + redisViewCount;
              return mapToMainReviewResponseDto(review, totalViewCount);
            })
            .sorted((first, second) -> Integer.compare(second.getViewCount(), first.getViewCount()))
            .collect(Collectors.toList());

    redisReviewService.setPopularReview(dtos, POPULAR_REVIEWS_CACHE_DURATION);
    return dtos;
  }

  // 인기 리뷰 (좋아요 순)
  @Override
  public List<MainReviewResponseDto> getPopularReviewsByLikes(HttpServletRequest request) {
    userService.findUserByToken(request);
    Object cached = redisReviewService.getPopularLikes();
    if (cached instanceof List) {
      return (List<MainReviewResponseDto>) cached;
    }

    List<Review> reviews = reviewRepository.findPopularByLikes();
    List<MainReviewResponseDto> likeDtos = reviews.stream()
            .map(review -> {
              int redisViewCount = getViewCount(review.getId());
              int totalViewCount = review.getViewCount() + redisViewCount;
              return mapToMainReviewResponseDto(review, totalViewCount);
            })
            .collect(Collectors.toList());

    redisReviewService.setPopularReviewByLikeKey(likeDtos, POPULAR_REVIEWS_CACHE_DURATION);
    return likeDtos;
  }

  // 리뷰 검색
  @Override
  public List<MainReviewResponseDto> searchReviews(HttpServletRequest request, String keyword) {
    User user= userService.findUserByToken(request); // 토큰 검증만 수행
    redisSearchService.addSearchTerm(user.getId().toString(), keyword);
    return reviewRepository.searchReview(keyword);
  }

  private MainReviewResponseDto mapToMainReviewResponseDto(Review review, int totalViewCount) {
    String category = null;
    String placeName = null;
    if (review.getReviewPlaces() != null && !review.getReviewPlaces().isEmpty()) {
      ReviewPlace firstReviewPlace = review.getReviewPlaces().iterator().next();
      Place place = firstReviewPlace.getPlace();
      if (place != null) {
        category = place.getCategory();
        placeName = place.getPlaceName();
      }
    }
    String nickName = review.getUser() != null ? review.getUser().getNickname() : null;
    String thumbnailUrl = null;
    if (review.getFiles() != null && !review.getFiles().isEmpty()) {
      thumbnailUrl = review.getFiles().iterator().next().getFileUrl();
    }
    return MainReviewResponseDto.builder()
            .reviewId(review.getId())
            .category(category)
            .placeName(placeName)
            .title(review.getTitle())
            .viewCount(totalViewCount)
            .likeCount(review.getLikeCount())
            .commentCount(review.getCommentCount())
            .nickName(nickName)
            .thumbnailUrl(thumbnailUrl)
            .build();
  }
}
