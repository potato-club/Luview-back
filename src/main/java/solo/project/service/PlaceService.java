package solo.project.service;

import solo.project.dto.place.request.PlaceRequestDto;
import solo.project.entity.Place;

import java.util.List;

public interface PlaceService {
  //장소 생성(db에 있으면 생성X)
  List<Place> createPlace(List<PlaceRequestDto> placeRequestDto);

}
