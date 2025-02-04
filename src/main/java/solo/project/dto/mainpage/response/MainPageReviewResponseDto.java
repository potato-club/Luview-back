package solo.project.dto.mainpage.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MainPageReviewResponseDto {
    private Long reviewId;
    private String storeName;
    private String title;
    private LocalDateTime createdAt;
    private String thumbnailUrl;

    @Builder
    public MainPageReviewResponseDto(Long reviewId,
                                     String storeName,
                                     String title,
                                     LocalDateTime createdAt,
                                     String thumbnailUrl) {
        this.reviewId = reviewId;
        this.storeName = storeName;
        this.title = title;
        this.createdAt = createdAt;
        this.thumbnailUrl = thumbnailUrl;
    }
}
