package solo.project.service;

import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.entity.Place;

import java.util.List;

public interface PlaceService {
  List<Place> createPlace(List<PlaceRequestDto> placeRequestDto);
  Place updatePlace(PlaceRequestDto placeRequestDto);
  boolean existPlace(PlaceRequestDto placeRequestDto);
}
