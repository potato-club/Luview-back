package solo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import solo.project.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
