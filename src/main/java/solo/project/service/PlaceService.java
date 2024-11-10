package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Place.request.PlaceRequestDto;
import solo.project.entity.Place;
import solo.project.repository.PlaceRepository;

@Service
@RequiredArgsConstructor
public class PlaceService {
  private final PlaceRepository placeRepository;

  public void createPlace(PlaceRequestDto placeRequestDto, HttpServletRequest request) {
    // 장소가 이미 존재하는지 확인 (예: kakaoPlaceId 기반으로 확인)
    Place existingPlace = placeRepository.findByKakaoPlaceId(placeRequestDto.getKakaoPlaceId());
    if (existingPlace != null) {
      // 이미 존재하는 경우 기존 장소를 반환하지 않고 메서드를 종료
      return;
    }

    // 존재하지 않으면 새 장소 저장
    placeRepository.save(Place.toEntity(placeRequestDto));
  }
}
