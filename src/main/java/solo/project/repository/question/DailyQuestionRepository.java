package solo.project.repository.question;

import org.springframework.data.jpa.repository.JpaRepository;
import solo.project.entity.DailyQuestion;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyQuestionRepository extends JpaRepository<DailyQuestion, Long> ,DailyQuestionRepositoryCustom{
    Optional<DailyQuestion> findByCouple_IdAndQuestionDate(Long coupleId, LocalDate questionDate);
}
