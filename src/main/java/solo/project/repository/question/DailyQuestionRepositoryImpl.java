package solo.project.repository.question;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import solo.project.entity.DailyQuestion;
import solo.project.entity.QDailyQuestion;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class DailyQuestionRepositoryImpl implements DailyQuestionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QDailyQuestion qDailyQuestion = QDailyQuestion.dailyQuestion;

    @Override
    public List<DailyQuestion> findQuestionsByDateRange(Long coupleId, LocalDate startDate, LocalDate endDate) {
        return queryFactory
                .selectFrom(qDailyQuestion)
                .where(qDailyQuestion.couple.id.eq(coupleId)
                        .and(qDailyQuestion.questionDate.between(startDate, endDate)))
                .orderBy(qDailyQuestion.questionDate.desc())
                .fetch();
    }
}
