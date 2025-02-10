package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.service.LikeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
@Tag(name = "Like Controller", description = "리뷰 좋아요 관련 API")
public class LikeController {

  private final LikeService likeService;

  @Operation(summary = "리뷰 좋아요 추가")
  @PostMapping("/like_add/{reviewId}")
  public ResponseEntity<String> addLike(@PathVariable Long reviewId, HttpServletRequest request) {
    likeService.addLike(reviewId, request);
    return ResponseEntity.ok("좋아요가 추가되었습니다.");
  }

  @Operation(summary = "리뷰 좋아요 취소")
  @DeleteMapping("/like_decr/{reviewId}")
  public ResponseEntity<String> removeLike(@PathVariable Long reviewId, HttpServletRequest request){
    likeService.removeLike(reviewId, request);
    return ResponseEntity.ok("좋아요가 취소되었습니다.");
  }
}
