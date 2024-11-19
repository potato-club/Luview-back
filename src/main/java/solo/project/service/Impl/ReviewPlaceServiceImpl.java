package solo.project.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.entity.Place;
import solo.project.entity.Review;
import solo.project.entity.ReviewPlace;
import solo.project.repository.PlaceRepository;
import solo.project.repository.ReviewPlaceRepository;
import solo.project.service.ReviewPlaceService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReviewPlaceServiceImpl implements ReviewPlaceService {
  private ReviewPlaceRepository reviewPlaceRepository;
  private PlaceRepository placeRepository;

  @Override
  public void createReviewPlaces(Review review, List<Place> places, List<PlaceRequestDto> placeRequestDtos) {
    List<ReviewPlace> reviewPlaces = IntStream.range(0, placeRequestDtos.size())
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


}
