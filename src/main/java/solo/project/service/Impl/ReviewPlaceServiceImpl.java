package solo.project.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.entity.Place;
import solo.project.entity.Review;
import solo.project.entity.ReviewPlace;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.repository.PlaceRepository;
import solo.project.repository.ReviewPlaceRepository;
import solo.project.service.ReviewPlaceService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewPlaceServiceImpl implements ReviewPlaceService {
  private final ReviewPlaceRepository reviewPlaceRepository;

  @Override
  public void createReviewPlaces(Review review, List<Place> places, List<PlaceRequestDto> placeRequestDtos) {
    List<ReviewPlace> reviewPlaces = IntStream.range(0, places.size())
        .mapToObj(i -> {
          PlaceRequestDto placeRequestDto = placeRequestDtos.get(i);
          Place place = places.get(i);  // 대응되는 장소
          return ReviewPlace.builder()
              .review(review)
              .place(place)
              .rating(placeRequestDto.getRating())  // DTO에서 별점 가져오기
              .build();
        })
        .collect(Collectors.toList());  // ReviewPlace 객체 리스트로 수집

    reviewPlaceRepository.saveAll(reviewPlaces);
  }

  @Override
  public List<Place> findPlacesByReview(Review review) {
    List<ReviewPlace> reviewPlaces = reviewPlaceRepository.findByReview(review);
    return reviewPlaces.stream().map(ReviewPlace::getPlace).toList();
  }

  @Override
  public void deleteReviewPlaces(Review review) {
    List<ReviewPlace> reviewPlace = reviewPlaceRepository.findByReview(review);
    if(reviewPlace.isEmpty()) {
      throw new NotFoundException("리뷰에 등록된 장소를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }
    reviewPlaceRepository.deleteAll(reviewPlace);
  }


}
