package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solo.project.dto.Review.request.ReviewRequestDto;
import solo.project.service.ReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/review")
@Tag(name="Review CRUD", description = "리뷰글 CRUD")
public class ReviewController {
  private final ReviewService reviewService;

  @Operation(summary = "리뷰글 작성")
  @PostMapping()
  public ResponseEntity<String> createReview(@RequestBody ReviewRequestDto reviewRequestDto, HttpServletRequest request) {
    reviewService.createReview(reviewRequestDto, request);
    return ResponseEntity.ok("리뷰글 작성 완료");
  }

}
