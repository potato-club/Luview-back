package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import solo.project.dto.file.FileRequestDto;
import solo.project.entity.*;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.ReviewPlaceRepository;
import solo.project.repository.review.ReviewRepository;
import org.springframework.data.domain.Pageable;
import solo.project.service.*;

import java.io.IOException;
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
    return reviewRepository.getReviewDetail(reviewId);
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
            .NickName(review.getUser().getNickname())
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
}

