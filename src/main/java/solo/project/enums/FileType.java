package solo.project.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {
    PROFILE("ROLE_PROFILE", "프로필 사진"),
    REVIEW("ROLE_REVIEW", "리뷰 사진");

    private final String key;
    private final String title;

}
