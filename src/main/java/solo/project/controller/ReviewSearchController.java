package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import solo.project.service.ReviewService;
import solo.project.service.redis.RedisSearchService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/review_search")
@Tag(name = "ReviewSearch Controller", description = "리뷰 검색 API")
public class ReviewSearchController {
    private final ReviewService reviewService;
    private final RedisSearchService redisSearchService;

    @Operation(summary = "메인 리뷰글 목록 조회")
    @GetMapping("/main")
    public ResponseEntity<List<MainReviewResponseDto>> getMainReview(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        List<MainReviewResponseDto> mainReviews = reviewService.getMainReviews(pageable);
        return ResponseEntity.ok(mainReviews);
    }

    @Operation(summary = "카테고리 정렬 목록 조회")
    @GetMapping("/{category}")
    public ResponseEntity<List<MainReviewResponseDto>> getMainReviewByCategory(
            @PathVariable String category,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        List<MainReviewResponseDto> categoryReviews = reviewService.getReviewsByCategory(category, pageable);
        return ResponseEntity.ok(categoryReviews);
    }

    @Operation(summary = "리뷰글 상세 조회")
    @GetMapping("/detail/{review_id}")
    public ResponseEntity<ReviewResponseDto> getReviewDetail(@PathVariable("review_id") Long reviewId) {
        ReviewResponseDto dto = reviewService.getReviewDetail(reviewId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "인기 리뷰 조회(조회순)")
    @GetMapping("/popular_check")
    public ResponseEntity<List<MainReviewResponseDto>> getPopularReviews(HttpServletRequest request) {
        List<MainReviewResponseDto> popularCheckReviews = reviewService.getPopularReviews(request);
        return ResponseEntity.ok(popularCheckReviews);
    }

    @Operation(summary = "인기 리뷰 조회(좋아요순)")
    @GetMapping("/popular_like")
    public ResponseEntity<List<MainReviewResponseDto>> getPopularReviewsLike(HttpServletRequest request) {
        List<MainReviewResponseDto> popularLikeReviews=reviewService.getPopularReviewsByLikes(request);
        return ResponseEntity.ok(popularLikeReviews);
    }

    @Operation(summary = "게시물(리뷰) 검색 " ,description = " 제목,내용,작성자 / 검색시 바로 최근검색어 갱신")
    @GetMapping("/search")
    public ResponseEntity<List<MainReviewResponseDto>> getSearchReviews(HttpServletRequest request,String keyword) {
        List<MainReviewResponseDto> reviewSearch=reviewService.searchReviews(request, keyword);
        return ResponseEntity.ok(reviewSearch);
    }
    //최근 검색어에 있는 동일한 값 입력시 가장 최상단으로 끌고옴

    @Operation(summary = "최근 검색어 조회" ,description = "사용자 최근 검색어 최신순")
    @GetMapping("/recent")
    public ResponseEntity<List<String>> getRecentSearchTerms(HttpServletRequest request) {
        List<String> terms=redisSearchService.getRecentSearchTerms(request);
        return ResponseEntity.ok(terms);
    }

    @Operation(summary = "최근 검색어 전체 삭제")
    @DeleteMapping("/clear_recent")
    public ResponseEntity<String> clearRecentSearchTerms(HttpServletRequest request) {
        String clearTerms =redisSearchService.clearRecentSearchTerms(request);
        return ResponseEntity.ok(clearTerms);
    }

}
