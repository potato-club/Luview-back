package solo.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "daily_question")
public class DailyQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 커플 엔티티와 다대일 연관관계 (각 DailyQuestion은 하나의 커플에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    // 질문 엔티티와 다대일 연관관계 (각 DailyQuestion은 하나의 Question을 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(length = 500)
    private String answerUser1;

    @Column(length = 500)
    private String answerUser2;

    @Column(nullable = false)
    private LocalDate questionDate;

    public DailyQuestion(Couple couple, Question question, LocalDate questionDate) {
        this.couple = couple;
        this.question = question;
        this.questionDate = questionDate;
    }

    public void setAnswerUser1(String answerUser1) {
        this.answerUser1 = answerUser1;
    }
    public void setAnswerUser2(String answerUser2) {
        this.answerUser2 = answerUser2;
    }
}
