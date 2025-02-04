package solo.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.mainpage.response.MainPageResponseDto;
import solo.project.dto.mainpage.response.MyInfoResponseDto;
import solo.project.dto.mainpage.response.PartnerInfoResponseDto;
import solo.project.dto.mainpage.response.MainPageReviewResponseDto;
import solo.project.repository.mainpage.MainPageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainPageService {

    private final MainPageRepository mainPageRepository;
    public MainPageResponseDto getMainPageData(Long userId, int reviewLimit) {
        // 내 정보 조회
        MyInfoResponseDto myInfo = mainPageRepository.getMyInfo(userId);
        // 파트너 정보 조회
        PartnerInfoResponseDto partnerInfo = mainPageRepository.getPartnerInfo(userId);
        // 커플 ID 및 커플 일수 조회 (커플이 존재하지 않으면 0으로 처리)
        Long coupleId = mainPageRepository.getCoupleIdByUserId(userId);
        Long coupleDays = (coupleId != null) ? mainPageRepository.getCoupleDays(coupleId) : 0L;
        // 최신 리뷰 목록 조회
        List<MainPageReviewResponseDto> recentReviews = mainPageRepository.getCoupleRecentReviews(userId, reviewLimit);

        return MainPageResponseDto.builder()
                .myInfo(myInfo)
                .partnerInfo(partnerInfo)
                .coupleDays(coupleDays)
                .recentReviews(recentReviews)
                .build();
    }
}
