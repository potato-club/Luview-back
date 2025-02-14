package solo.project.repository.file;

import org.springframework.data.jpa.repository.JpaRepository;
import solo.project.entity.File;
import solo.project.entity.Review;
import solo.project.entity.User;
import solo.project.enums.FileType;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> , FileRepositoryCustom {
    List<File> findByReview(Review review);

    List<File> findByUser(User user);

    List<File> findByUserAndFileType(User user, FileType fileType);

    List<File> findByReviewAndFileType(Review review, FileType fileType);
}
