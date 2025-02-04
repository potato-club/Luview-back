package solo.project.dto.mainpage.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * "상대방 정보" 조회용 Response DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PartnerInfoResponseDto {
    private Long userId;
    private String nickname;
    private String profileUrl;

    @Builder
    public PartnerInfoResponseDto(Long userId, String nickname, String profileUrl) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
    }
}
