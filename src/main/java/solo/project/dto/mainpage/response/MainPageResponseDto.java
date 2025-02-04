package solo.project.dto.mainpage.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MainPageResponseDto {
    private MyInfoResponseDto myInfo;
    private PartnerInfoResponseDto partnerInfo;
    private Long coupleDays;
    private List<MainPageReviewResponseDto> recentReviews;

    @Builder
    public MainPageResponseDto(MyInfoResponseDto myInfo,
                               PartnerInfoResponseDto partnerInfo,
                               Long coupleDays,
                               List<MainPageReviewResponseDto> recentReviews) {
        this.myInfo = myInfo;
        this.partnerInfo = partnerInfo;
        this.coupleDays = coupleDays;
        this.recentReviews = recentReviews;
    }
}
