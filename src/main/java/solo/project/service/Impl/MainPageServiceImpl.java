package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.mainpage.response.MainPageResponseDto;
import solo.project.dto.mainpage.response.MyInfoResponseDto;
import solo.project.dto.mainpage.response.PartnerInfoResponseDto;
import solo.project.dto.mainpage.response.MainPageReviewResponseDto;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.mainpage.MainPageRepository;
import solo.project.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainPageServiceImpl {

    private final MainPageRepository mainPageRepository;
    private final UserService userService;

    public MainPageResponseDto getMainPageData(HttpServletRequest request, int reviewLimit) {
        User user = userService.findUserByToken(request);
        Long userId = user.getId();

        if (userId == null) {
            throw new UnAuthorizedException("유효한 사용자 ID가 제공되지 않았습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        MyInfoResponseDto myInfo = mainPageRepository.getMyInfo(userId);
        if (myInfo == null) {
            throw new NotFoundException("내 정보가 조회되지 않습니다. userId=" + userId, ErrorCode.NOT_FOUND_EXCEPTION);
        }

        PartnerInfoResponseDto partnerInfo = mainPageRepository.getPartnerInfo(userId);

        Long coupleId = mainPageRepository.getCoupleIdByUserId(userId);
        Long coupleDays = (coupleId != null) ? mainPageRepository.getCoupleDays(coupleId) : 0L;

        List<MainPageReviewResponseDto> recentReviews = mainPageRepository.getCoupleRecentReviews(userId, reviewLimit);

        return MainPageResponseDto.builder()
                .myInfo(myInfo)
                .partnerInfo(partnerInfo)
                .coupleDays(coupleDays)
                .recentReviews(recentReviews)
                .build();
    }
}
