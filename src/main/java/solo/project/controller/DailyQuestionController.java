package solo.project.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import solo.project.dto.question.request.AnswerRequest;
import solo.project.dto.question.response.DailyQuestionResponseDto;
import solo.project.service.DailyQuestionService;

import java.util.List;

@RestController
@RequestMapping("/daily_question")
@RequiredArgsConstructor
@Tag(name = "Daily Question Controller" ,description = "매일 새로 받는 커플 질문")
public class DailyQuestionController {

    private final DailyQuestionService dailyQuestionService;

    @Operation(summary = "당일 질문 조회 API")
    @GetMapping("/check")
    public ResponseEntity<DailyQuestionResponseDto> getTodayQuestion(HttpServletRequest request) {
        DailyQuestionResponseDto dto = dailyQuestionService.getTodayQuestionForCurrentUser(request);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "답변 제출 API")
    @PostMapping("/answer")
    public ResponseEntity<String> submitAnswer(HttpServletRequest request, @RequestBody AnswerRequest req) {
        dailyQuestionService.submitAnswerForCurrentUser(request, req.getAnswer());
        return ResponseEntity.ok("답변이 제출되었습니다.");
    }

    @Operation(summary = "지정일 질문 이력 조회 API")
    @GetMapping("/history")
    public ResponseEntity<List<DailyQuestionResponseDto>> getHistory(HttpServletRequest request, @RequestParam("YYYY-MM-DD") String date) {
        List<DailyQuestionResponseDto> history = dailyQuestionService.getHistoryForCurrentUser(request, date);
        return ResponseEntity.ok(history);
    }
}
