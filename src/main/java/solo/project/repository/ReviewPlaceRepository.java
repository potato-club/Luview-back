package solo.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import solo.project.entity.Review;
import solo.project.entity.ReviewPlace;

@Repository
public interface ReviewPlaceRepository extends CrudRepository<ReviewPlace, Long> {
}
