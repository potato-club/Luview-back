package solo.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import solo.project.entity.Review;
import solo.project.entity.ReviewPlace;

import java.util.List;

@Repository
public interface ReviewPlaceRepository extends CrudRepository<ReviewPlace, Long> {
  List<ReviewPlace> findByReview(Review review);
}
