package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import solo.project.dto.question.response.DailyQuestionResponseDto;
import solo.project.entity.Couple;
import solo.project.entity.DailyQuestion;
import solo.project.entity.Question;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.repository.CoupleRepository;
import solo.project.repository.QuestionRepository;
import solo.project.repository.question.DailyQuestionRepository;
import solo.project.service.DailyQuestionService;
import solo.project.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyQuestionServiceImpl implements DailyQuestionService {

    private final DailyQuestionRepository dailyQuestionRepository;
    private final CoupleRepository coupleRepository;
    private final QuestionRepository questionRepository;
    private final UserService userService;

    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    public void resetDailyQuestions() {
        LocalDate today = LocalDate.now();
        List<Couple> couples = coupleRepository.findAll();
        for (Couple couple : couples) {
            if (dailyQuestionRepository.findByCouple_IdAndQuestionDate(couple.getId(), today).isPresent()) {
                continue;
            }
            DailyQuestion dq = DailyQuestion.builder()
                    .couple(couple)
                    .question(getRandomQuestion())
                    .questionDate(today)
                    .build();
            dailyQuestionRepository.save(dq);
        }
    }

    @Override
    public DailyQuestion getOrCreateTodayQuestionForCouple(Long coupleId) {
        LocalDate today = LocalDate.now();
        Optional<DailyQuestion> dqOpt = dailyQuestionRepository.findByCouple_IdAndQuestionDate(coupleId, today);
        if (dqOpt.isPresent()) {
            return dqOpt.get();
        }
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new NotFoundException("커플 정보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));
        DailyQuestion newDQ = DailyQuestion.builder()
                .couple(couple)
                .question(getRandomQuestion())
                .questionDate(today)
                .build();
        return dailyQuestionRepository.save(newDQ);
    }

    @Override
    public DailyQuestionResponseDto convertToDto(DailyQuestion dq) {
        return DailyQuestionResponseDto.builder()
                .id(dq.getId())
                .questionContent(dq.getQuestion().getQuestion())
                .answerUser1(dq.getAnswerUser1())
                .answerUser2(dq.getAnswerUser2())
                .questionDate(dq.getQuestionDate())
                .build();
    }

    @Override
    public void submitAnswer(Long coupleId, Long userId, String answer) {
        DailyQuestion dq = getOrCreateTodayQuestionForCouple(coupleId);
        Couple couple = dq.getCouple();
        if (couple.getUser1().getId().equals(userId)) {
            dq.setAnswerUser1(answer);
        } else if (couple.getUser2().getId().equals(userId)) {
            dq.setAnswerUser2(answer);
        } else {
            throw new NotFoundException("사용자가 해당 커플에 속해있지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }
        dailyQuestionRepository.save(dq);
    }

    @Override
    public List<DailyQuestionResponseDto> getQuestionsByDateRange(Long coupleId, LocalDate startDate, LocalDate endDate) {
        List<DailyQuestion> list = dailyQuestionRepository.findQuestionsByDateRange(coupleId, startDate, endDate);
        return list.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public DailyQuestionResponseDto getTodayQuestionForCurrentUser(HttpServletRequest request) {
        Couple couple = getCurrentCouple(request);
        DailyQuestion dq = getOrCreateTodayQuestionForCouple(couple.getId());
        return convertToDto(dq);
    }

    @Override
    public void submitAnswerForCurrentUser(HttpServletRequest request, String answer) {
        Couple couple = getCurrentCouple(request);
        User user = userService.findUserByToken(request);
        submitAnswer(couple.getId(), user.getId(), answer);
    }

    @Override
    public List<DailyQuestionResponseDto> getHistoryForCurrentUser(HttpServletRequest request, String date) {
        Couple couple = getCurrentCouple(request);
        LocalDate targetDate = LocalDate.parse(date);
        return getQuestionsByDateRange(couple.getId(), targetDate, targetDate);
    }

    // 현재 요청의 사용자 정보를 기반으로 커플 정보를 조회하는 공통 메서드
    private Couple getCurrentCouple(HttpServletRequest request) {
        User user = userService.findUserByToken(request);
        return coupleRepository.findByUser1OrUser2(user, user)
                .orElseThrow(() -> new NotFoundException("커플 정보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));
    }

    private Question getRandomQuestion() {
        List<Question> questions = questionRepository.findAll();
        if (questions.isEmpty()) {
            throw new NotFoundException("질문 풀이 비어있습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }
        Random random = new Random();
        return questions.get(random.nextInt(questions.size()));
    }
}
