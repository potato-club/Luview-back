package solo.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.entity.Place;
import solo.project.entity.Review;
import solo.project.entity.ReviewPlace;
import solo.project.repository.PlaceRepository;
import solo.project.repository.ReviewPlaceRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewPlaceServiceImpl implements ReviewPlaceService {
  private ReviewPlaceRepository reviewPlaceRepository;
  private PlaceRepository placeRepository;
  @Override
  public void createReviewPlace(Review review, List<Place> places, int rating) {


    ReviewPlace reviewPlace = new ReviewPlace(review, places, places.getRating());
    reviewPlaceRepository.save(reviewPlace);
  }

  @Override
  public void updateReviewPlace(Review review, PlaceRequestDto placeRequestDto) {
    // 리뷰에 연결된 기존 ReviewPlace 목록 조회
    List<ReviewPlace> currentReviewPlaces = reviewPlaceRepository.findByReview(review);

    // KakaoPlaceId로 현재 연결된 리뷰장소를 찾기
    Optional<ReviewPlace> existingReviewPlaceOptional = currentReviewPlaces.stream()
        .filter(reviewPlace -> reviewPlace.getPlace().getKakaoPlaceId().equals(placeRequestDto.getKakaoPlaceId()))
        .findFirst();

    if (existingReviewPlaceOptional.isPresent()) {
      // 기존에 연결된 ReviewPlace가 있다면
      ReviewPlace existingReviewPlace = existingReviewPlaceOptional.get();

      // 장소 정보가 변경되었을 경우 업데이트
      if (!existingReviewPlace.getPlace().equals(place)) {
        existingReviewPlace.updatePlace(place, );
        reviewPlaceRepository.save(existingReviewPlace); // 변경사항 저장
      }
    } else {
      // 기존에 연결된 ReviewPlace가 없으면 새로 추가
      ReviewPlace newReviewPlace = new ReviewPlace();
      newReviewPlace.setReview(review);
      newReviewPlace.setPlace(place);

      reviewPlaceRepository.save(newReviewPlace);
    }
  }
}
