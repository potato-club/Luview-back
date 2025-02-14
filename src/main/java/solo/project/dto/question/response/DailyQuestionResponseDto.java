package solo.project.dto.question.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DailyQuestionResponseDto {
    private Long id;
    private String questionContent;      // Question 엔티티의 content
    private String answerUser1;
    private String answerUser2;
    private LocalDate questionDate;
}
