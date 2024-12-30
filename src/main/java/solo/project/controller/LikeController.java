package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solo.project.service.LikeService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/like")
@Tag(name="Like Controller", description = "좋아요 API")
public class LikeController {
  private final LikeService likeService;

  @Operation(summary = "좋아요 생성 및 삭제")
  @PostMapping("/{review_id}")
  public ResponseEntity<String> like(@PathVariable Long review_id, HttpServletRequest request) {
    return ResponseEntity.ok(likeService.likeReview(review_id, request));
  }


}
