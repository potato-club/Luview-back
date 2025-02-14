package solo.project.dto.reviewPlace.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPlaceResponseDto {
    private Long placeId;
    private String placeName;
    private String addressName;
    private String category;
    private int rating;  // 별점
}

