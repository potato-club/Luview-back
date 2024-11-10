package solo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solo.project.entity.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
  Place findByKakaoPlaceId(String kakaoPlaceId);
}
