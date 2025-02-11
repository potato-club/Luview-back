package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.dto.file.FileRequestDto;
import solo.project.service.ReviewService;
import solo.project.service.redis.RedisSearchService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/review")
@Tag(name = "Review Controller", description = "리뷰글 API")
public class ReviewController {

  private final ReviewService reviewService;
  private final RedisSearchService redisSearchService;

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

}

