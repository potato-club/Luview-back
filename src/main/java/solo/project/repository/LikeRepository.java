package solo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import solo.project.entity.Like;
import solo.project.entity.Review;
import solo.project.entity.User;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
  Like findByUserAndReview(User user, Review review);
}
