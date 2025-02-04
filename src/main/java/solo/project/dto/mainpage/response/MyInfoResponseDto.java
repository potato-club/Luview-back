package solo.project.dto.mainpage.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * "내 정보" (프로필) 조회용 Response DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MyInfoResponseDto {
    private Long userId;
    private String nickname;
    private String profileUrl;

    @Builder
    public MyInfoResponseDto(Long userId, String nickname, String profileUrl) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
    }
}
