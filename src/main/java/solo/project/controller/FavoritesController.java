package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.service.FavoritesService;

@RestController
@RequiredArgsConstructor
@RequestMapping("favorites")
public class FavoritesController {
  private final FavoritesService favoritesService;

  @Operation(summary = "즐겨찾기 추가")
  @PostMapping("{review_id}")
  public ResponseEntity<String> createFavorites(@PathVariable Long review_id, HttpServletRequest request) {
    favoritesService.createFavorite(review_id, request);
    return ResponseEntity.ok("즐겨찾기 추가 성공");
  }

  @Operation(summary = "즐겨찾기 삭제")
  @DeleteMapping("{review_id}")
  public ResponseEntity<String> deleteFavorites(@PathVariable Long review_id, HttpServletRequest request) {
    favoritesService.deleteFavorite(review_id, request);
    return ResponseEntity.ok("즐겨찾기 삭제 성공");
  }

}
