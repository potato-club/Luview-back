package solo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solo.project.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

}
