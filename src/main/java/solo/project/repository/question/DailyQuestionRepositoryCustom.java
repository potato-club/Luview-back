package solo.project.repository.question;

import solo.project.entity.DailyQuestion;

import java.time.LocalDate;
import java.util.List;

public interface DailyQuestionRepositoryCustom {
    List<DailyQuestion> findQuestionsByDateRange(Long coupleId, LocalDate startDate, LocalDate endDate);
}
