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
import solo.project.dto.jwt.JwtTokenProvider;
import solo.project.entity.*;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.ReviewPlaceRepository;
import solo.project.repository.review.ReviewRepository;
import org.springframework.data.domain.Pageable;
import solo.project.service.*;
import solo.project.service.redis.RedisReviewListService;
import solo.project.service.redis.RedisCountSyncService;

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
  private final ReviewRepository reviewRepository; // ★주의: reviewRepository가 Custom 구현도 갖고 있음
  private final PlaceService placeService;
  private final ReviewPlaceService reviewPlaceService;
  private final ReviewPlaceRepository reviewPlaceRepository;
  private final ImageService imageService;
  private static final String REVIEW_VIEW_COUNT_KEY_PREFIX="review:view:";
  private final RedisTemplate<String,Object> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  //1시간 지정
  private static final Duration POPULAR_REVIEWS_CACHE_DURATION = Duration.ofMinutes(5);
  private final RedisReviewListService redisReviewService;
  private final RedisCountSyncService redisViewCountSyncService;


  @Override
  public void createReview(ReviewRequestDto reviewRequestDto, HttpServletRequest request, List<MultipartFile> files) throws IOException {
    // 1) 사용자 인증
    User user = userService.findUserByToken(request);
    if (user == null)
      throw new UnAuthorizedException("로그인 후 리뷰글 작성 가능", ErrorCode.UNAUTHORIZED_EXCEPTION);

    // 2) 장소 필수 검사
    if (reviewRequestDto.getPlaces() == null || reviewRequestDto.getPlaces().isEmpty())
      throw new NotFoundException("장소를 등록해주세요!", ErrorCode.NOT_FOUND_EXCEPTION);

    // 3) 리뷰 생성
    Review review = Review.builder()
            .user(user)
            .title(reviewRequestDto.getTitle())
            .content(reviewRequestDto.getContent())
            .viewCount(0)
            .likeCount(0)
            .commentCount(0)
            .build();
    Review savedReview = reviewRepository.save(review);

    // 4) 장소 생성 + 리뷰-장소 연결
    List<Place> places = placeService.createPlace(reviewRequestDto.getPlaces());
    reviewPlaceService.createReviewPlaces(savedReview, places, reviewRequestDto.getPlaces());

    // 5) 이미지 업로드
    if (files != null && !files.isEmpty()) {
      imageService.uploadImages(files, savedReview);
    }
  }

  @Override
  public List<MainReviewResponseDto> getMainReviews(Pageable pageable) {
    // 단순 최신순 목록
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

  /**
   * 리뷰 상세 조회
   *  -> ReviewRepositoryCustom.getReviewDetail(reviewId) 호출
   */
  @Override
  @Transactional(readOnly = true)
  public ReviewResponseDto getReviewDetail(Long reviewId) {
    ReviewResponseDto reviewDetail= reviewRepository.getReviewDetail(reviewId);

    //Redis 이용한 조회수 증가 - 멀티쓰레지 방지를 위해 사용
    incrementViewCount(reviewId);

    int redisViewCount= getViewCount(reviewId);
    int totalViewCount = reviewDetail.getViewCount() + redisViewCount;
    reviewDetail.setViewCount(totalViewCount);

    return reviewDetail;
  }

  @Override
  public void updateReview(Long id, ReviewRequestDto reviewRequestDto, HttpServletRequest request, List<MultipartFile> newFiles, List<FileRequestDto> deleteFiles) throws IOException {
    User user = userService.findUserByToken(request);
    if(user == null)
      throw new UnAuthorizedException("리뷰글 수정 권한이 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);

    Review review = reviewRepository.findById(id).orElse(null);
    if (review == null)
      throw new NotFoundException("수정할 수 없는 리뷰글입니다", ErrorCode.NOT_FOUND_EXCEPTION);
    if (review.getUser() != user)
      throw new UnAuthorizedException("리뷰글 수정 권한이 없습니다", ErrorCode.UNAUTHORIZED_EXCEPTION);

    // 제목/내용 수정
    review.update(reviewRequestDto);

    // 장소 수정
    List<ReviewPlace> oldPlace = reviewPlaceRepository.findByReview(review);
    List<PlaceRequestDto> newPlace = reviewRequestDto.getPlaces();
    if(hasChanges(oldPlace, newPlace)) {
      reviewPlaceService.deleteReviewPlaces(review);
      List<Place> places = placeService.createPlace(newPlace);
      reviewPlaceService.createReviewPlaces(review, places, newPlace);
    }

    // 이미지 업데이트
    imageService.updateImages(review, newFiles, deleteFiles);
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


  // 아래 메서드는 간단한 메인 목록 DTO 변환용
  private MainReviewResponseDto createMainReviewDto(Review review, String category) {
    Place place = null;
    if (category == null || category.isEmpty()) {
      place = review.getReviewPlaces().get(0).getPlace();
    } else {
      for (ReviewPlace rp : review.getReviewPlaces()) {
        if (rp.getPlace().getCategory().equals(category)) {
          place = rp.getPlace();
        }
      }
    }
    if (place == null) {
      throw new IllegalArgumentException("해당 카테고리 또는 리뷰에 대한 장소를 찾을 수 없습니다.");
    }

    // 첫 번째 이미지를 썸네일
    List<File> fileList = new ArrayList<>(review.getFiles());
    String thumbUrl = fileList.isEmpty() ? null : fileList.get(0).getFileUrl();


    return MainReviewResponseDto.builder()
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

  //리뷰 조회수 증가

  @Override
  public void incrementViewCount(Long reviewId) {
    String key = REVIEW_VIEW_COUNT_KEY_PREFIX + reviewId;
    ValueOperations<String, Object> ops = redisTemplate.opsForValue();
    ops.increment(key, 1);
  }

  //Redis저장된 조회수를 가져옴

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
  } // 뷰 카운트 코드인데 가독성이 너무 좋지않아 나중에 수정 예정 RedisCode 수정해야함

  @Override
  public List<MainReviewResponseDto> getPopularReviews(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveAccessToken(request);
    if (token == null) {
      throw new UnAuthorizedException("토큰이 존재하지 않습니다.", ErrorCode.INVALID_TOKEN_EXCEPTION);
    }
    Object cached = redisReviewService.getPopularReviews();
    if (cached instanceof List) {
      return (List<MainReviewResponseDto>) cached;
    }

    List<Review> reviews = reviewRepository.findPopularReview();
    // 각 리뷰에 대해 DB 조회수와 Redis 조회수를 합산한 후 DTO로 변환
    List<MainReviewResponseDto> dtos = reviews.stream()
            .map(review -> {
              int redisViewCount = getViewCount(review.getId());
              int totalViewCount = review.getViewCount() + redisViewCount;
              return mapToMainReviewResponseDto(review, totalViewCount);
            })
            .sorted((firstReviewDto, secondReviewDto) ->
                    Integer.compare(secondReviewDto.getViewCount(), firstReviewDto.getViewCount())
            )
            .collect(Collectors.toList());

    redisReviewService.setPopularReview(dtos, POPULAR_REVIEWS_CACHE_DURATION);
    return dtos;
  }

  //좋아요순 정렬
  @Override
  public List<MainReviewResponseDto> getPopularReviewsByLikes(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveAccessToken(request);
    if (token == null) {
      throw new UnAuthorizedException("토큰이 존재하지 않습니다." , ErrorCode.UNAUTHORIZED_EXCEPTION);
    }
    //Redis에서 조회
    Object cached = redisReviewService.getPopularLikes();
    if(cached instanceof List) {
      return (List<MainReviewResponseDto>) cached;
    }
    //없으면 DB순
    List<Review> reviews = reviewRepository.findPopularByLikes();
    List<MainReviewResponseDto> LikeDtos = reviews.stream()
            .map(review -> {
              int redisViewCount = getViewCount(review.getId());
              int totalViewCount = review.getViewCount() + redisViewCount;
              return mapToMainReviewResponseDto(review, totalViewCount);
            })
            .collect(Collectors.toList());


    redisReviewService.setPopularReviewByLikeKey(LikeDtos, POPULAR_REVIEWS_CACHE_DURATION);
    return LikeDtos;
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

