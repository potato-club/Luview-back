package solo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solo.project.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
}
