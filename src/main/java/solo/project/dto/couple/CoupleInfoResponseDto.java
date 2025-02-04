package solo.project.dto.couple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoupleInfoResponseDto {
    private Long myId;
    private String myNickname;
    private String myProfileUrl;

    // 상대 정보
    private Long partnerId;
    private String partnerNickname;
    private String partnerProfileUrl;

    // 커플 며칠째
    private long coupleDays;

    // 최근 리뷰 2개
    private List<RecentReviewDto> recentReviews;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentReviewDto {
        private Long reviewId;
        private String storeName;      // 예: "이태원 모수"
        private String title;          // 예: "주말 데이트"
        private LocalDateTime createdAt;
        private String thumbnailUrl;   // 리뷰 썸네일
    }
}
