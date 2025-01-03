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
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.service.ReviewService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/review")
@Tag(name="Review Controller", description = "리뷰글 API")
public class ReviewController {
  private final ReviewService reviewService;

  @Operation(summary = "리뷰글 작성")
  @PostMapping()
  public ResponseEntity<String> createReview(@RequestBody ReviewRequestDto reviewRequestDto, HttpServletRequest request) {
    reviewService.createReview(reviewRequestDto, request);
    return ResponseEntity.ok("리뷰글 작성 완료");
  }

  @Operation(summary = "메인 리뷰글 목록 조회")
  @GetMapping("/main")
  public ResponseEntity<List<MainReviewResponseDto>> getMainReview(@ParameterObject
                                                                     @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
                                                                     Pageable pageable) {
    List<MainReviewResponseDto> mainReviews= reviewService.getMainReviews(pageable);
    return ResponseEntity.ok(mainReviews);
  }

  @Operation(summary = "카테고리 정렬 목록 조회")
  @GetMapping("/{category}")
  public ResponseEntity<List<MainReviewResponseDto>> getMainReviewByCategory(@PathVariable String category,
                                                                             @ParameterObject
                                                                             @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
                                                                             Pageable pageable) {
    List<MainReviewResponseDto> categoryReviews = reviewService.getReviewsByCategory(category, pageable);
    return ResponseEntity.ok(categoryReviews);
  }

  @Operation(summary = "리뷰글 수정")
  @PutMapping("/{id}")
  public ResponseEntity<String> updateReview(@PathVariable Long id,
                                             @RequestBody ReviewRequestDto reviewRequestDto,
                                             HttpServletRequest request) {
    reviewService.updateReview(id, reviewRequestDto, request);
    return ResponseEntity.ok("리뷰글 수정 완료");
  }

  @Operation(summary = "리뷰글 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteReview(@PathVariable Long id,
                                             @RequestBody ReviewRequestDto reviewRequestDto,
                                             HttpServletRequest request) {
    reviewService.deleteReview(id, request);
    return ResponseEntity.ok("리뷰글 삭제 완료");
  }
}
