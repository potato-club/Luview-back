package solo.project.repository.File;

import org.springframework.data.jpa.repository.JpaRepository;
import solo.project.entity.File;
import solo.project.entity.Review;
import solo.project.entity.User;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> , FileRepositoryCustom {
    List<File> findByReview(Review review);

    List<File> findByUser(User user);
}
