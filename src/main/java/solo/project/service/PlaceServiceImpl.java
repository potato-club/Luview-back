package solo.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.entity.Place;
import solo.project.repository.PlaceRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {
  private final PlaceRepository placeRepository;

  @Override
  public List<Place> createPlace(List<PlaceRequestDto> placeRequestDtos) {
    List<Place> places = new ArrayList<>();
    for (PlaceRequestDto placeRequestDto : placeRequestDtos) {
      Place existingPlace = placeRepository.findByKakaoPlaceId(placeRequestDto.getKakaoPlaceId());
      if (existingPlace != null) {
        // 이미 존재하는 경우 그 장소 반환
        places.add(existingPlace);
      } else {
        places.add(Place.toEntity(placeRequestDto));
      }
    }

    return placeRepository.saveAll(places);
  }

  public Place updatePlace(PlaceRequestDto placeRequestDto) {
    return Place.builder().build();// 구현 해야 됨
  }

  public boolean existPlace(PlaceRequestDto placeRequestDto) {
    return placeRepository.findByKakaoPlaceId(placeRequestDto.getKakaoPlaceId()) != null;
  }

}
