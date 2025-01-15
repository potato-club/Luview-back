package solo.project.repository.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import solo.project.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> ,ReviewRepositoryCustom {
  Page<Review> findAllByOrderByCreatedDateDesc(Pageable pageable);

  @Query("SELECT r FROM Review r JOIN r.reviewPlaces rp JOIN rp.place p WHERE p.category = :category")
  Page<Review> findByPlaceCategory(@Param("category") String Category, Pageable pageable);
}
