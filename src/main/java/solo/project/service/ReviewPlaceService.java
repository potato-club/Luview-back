package solo.project.service;

import solo.project.dto.place.request.PlaceRequestDto;
import solo.project.entity.Place;
import solo.project.entity.Review;

import java.util.List;

public interface ReviewPlaceService {
  void createReviewPlaces(Review review, List<Place> places, List<PlaceRequestDto> placeRequestDtos);
  List<Place> findPlacesByReview(Review review);
  void deleteReviewPlaces(Review review);
}
