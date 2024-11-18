package solo.project.service;

import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.entity.Place;
import solo.project.entity.Review;

import java.util.List;

public interface ReviewPlaceService {
  void createReviewPlace(Review review, List<Place> places, int rating);
  void updateReviewPlace(Review review, PlaceRequestDto placeRequestDto);
}
