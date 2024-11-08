package solo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import solo.project.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

}
