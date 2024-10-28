package solo.project.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("ROLE_USER"," 일반 사용자"),
    MANAGER("ROLE_MANAGER","가게 주인"),
    ADMIN("ROLE_ADMIN","관리자");

    private final String key;
    private final String title;
}
