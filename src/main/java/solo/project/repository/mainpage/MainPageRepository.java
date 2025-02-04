package solo.project.repository.mainpage;

import solo.project.dto.mainpage.response.MainPageReviewResponseDto;
import solo.project.dto.mainpage.response.MyInfoResponseDto;
import solo.project.dto.mainpage.response.PartnerInfoResponseDto;

import java.util.List;

public interface MainPageRepository {
    MyInfoResponseDto getMyInfo(Long myUserId);
    PartnerInfoResponseDto getPartnerInfo(Long myUserId);
    Long getCoupleIdByUserId(Long myUserId);    // 커플아이디 가져오기
    Long getCoupleDays(Long coupleId);          // 커플 며칠째 계산용(예: createdAt diff)
    List<MainPageReviewResponseDto> getCoupleRecentReviews(Long myUserId, int limit);
}
