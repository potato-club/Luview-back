package solo.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.entity.Place;

@Service
@RequiredArgsConstructor
public class PlaceService {

  public Place createPlace(Place place) {
    return place;
  }
}
