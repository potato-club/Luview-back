package solo.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solo.project.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
