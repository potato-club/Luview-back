package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import solo.project.dto.question.response.DailyQuestionResponseDto;
import solo.project.entity.DailyQuestion;

import java.time.LocalDate;
import java.util.List;

public interface DailyQuestionService {
    DailyQuestion getOrCreateTodayQuestionForCouple(Long coupleId);
    DailyQuestionResponseDto getTodayQuestionForCurrentUser(HttpServletRequest request);
    DailyQuestionResponseDto convertToDto(DailyQuestion dq);
    void submitAnswer(Long coupleId, Long userId, String answer);
    void submitAnswerForCurrentUser(HttpServletRequest request, String answer);
    List<DailyQuestionResponseDto> getHistoryForCurrentUser(HttpServletRequest request, String date);
    List<DailyQuestionResponseDto> getQuestionsByDateRange(Long coupleId, LocalDate startDate, LocalDate endDate);

}

