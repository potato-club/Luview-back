package Couplace.token;

import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_tokens_id")
    private Long id;

    @Column(name = "users_uuid", columnDefinition = "BINARY(16)", unique = true)
    @NotNull  // 메시지 속성 생략
    private UUID userId;

    @Column(name = "token", nullable = false)
    @NotNull  // 메시지 속성 생략
    private String token;

    @Column(name = "refresh_token", nullable = false)
    @NotNull  // 메시지 속성 생략
    private String refreshToken;

    // 빌더 패턴에도 null 검증 및 예외 처리 추가
    @Builder
    public RefreshToken(UUID userId, String token, String refreshToken) {
        if (userId == null || token == null || refreshToken == null) {
            throw new IllegalArgumentException("UserId, Token, and RefreshToken cannot be null");
        }
        this.userId = userId;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    // refreshToken 값 업데이트 메서드, 예외 처리 추가
    public RefreshToken update(String newRefreshToken) {
        if (newRefreshToken == null) {
            throw new IllegalArgumentException("New RefreshToken cannot be null");
        }
        this.refreshToken = newRefreshToken;
        return this;
    }
}
