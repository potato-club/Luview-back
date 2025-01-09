package solo.project.repository;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import solo.project.entity.Favorites;
import solo.project.entity.Review;
import solo.project.entity.User;

import java.util.List;
import java.util.Optional;

public interface FavoritesRepository extends JpaRepository<Favorites, Long> {
  Optional<Favorites> findByUserAndReview(User user, Review review);
  List<Favorites> findByUser(User user);
}
