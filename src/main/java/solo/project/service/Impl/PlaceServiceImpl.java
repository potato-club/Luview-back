package solo.project.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.entity.Place;
import solo.project.repository.PlaceRepository;
import solo.project.service.PlaceService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {
  private final PlaceRepository placeRepository;

  @Override
  public List<Place> createPlace(List<PlaceRequestDto> placeRequestDtos) {
    List<Place> places = placeRequestDtos.stream()
        .map(placeRequestDto -> {
          Place existingPlace = placeRepository.findByKakaoPlaceId(placeRequestDto.getKakaoPlaceId());
          if (existingPlace != null) {
            return existingPlace;
          } else {
            return Place.toEntity(placeRequestDto);
          }
        })
        .collect(Collectors.toList());

    return placeRepository.saveAll(places);
  }



}
