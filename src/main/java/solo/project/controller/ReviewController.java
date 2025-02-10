package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.Review.response.MainReviewResponseDto;
import solo.project.dto.Review.response.ReviewResponseDto;
import solo.project.dto.file.FileRequestDto;
import solo.project.service.ReviewService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/review")
@Tag(name = "Review Controller", description = "리뷰글 API")
public class ReviewController {

  private final ReviewService reviewService;

  @Operation(summary = "리뷰글 작성")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> createReview(
          @RequestPart("requestDto") ReviewRequestDto reviewRequestDto,
          @RequestPart(value = "files", required = false) List<MultipartFile> files,
          HttpServletRequest request
  ) throws IOException {
    reviewService.createReview(reviewRequestDto, request, files);
    return ResponseEntity.ok("리뷰 작성 완료");
  }

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

  @Operation(summary = "리뷰글 수정")
  @PutMapping(value = "/{review_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> updateReview(
          @PathVariable Long review_id,
          @RequestPart("requestDto") ReviewRequestDto reviewRequestDto,
          @RequestPart(value = "files", required = false) List<MultipartFile> newFiles,
          @RequestPart(value = "deleteDto", required = false) List<FileRequestDto> deleteFiles,
          HttpServletRequest request
  ) throws IOException {
    reviewService.updateReview(review_id, reviewRequestDto, request, newFiles, deleteFiles);
    return ResponseEntity.ok("리뷰 수정 완료");
  }

  @Operation(summary = "리뷰글 삭제")
  @DeleteMapping("/{review_id}")
  public ResponseEntity<String> deleteReview(
          @PathVariable Long review_id,
          @RequestBody ReviewRequestDto reviewRequestDto,
          HttpServletRequest request
  ) {
    reviewService.deleteReview(review_id, request);
    return ResponseEntity.ok("리뷰글 삭제 완료");
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
}

