package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.entity.Couple;
import solo.project.service.CoupleService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/couple")
@Tag(name="couple Controller", description = "커플 연동 API")
public class CoupleController {

  private final CoupleService coupleService;

  @Operation(summary = "커플 연동")
  @PostMapping("/{uniqueCode}")
  public ResponseEntity<String> couple(@PathVariable String uniqueCode, HttpServletRequest request) {
    coupleService.createCouple(uniqueCode, request);
    return ResponseEntity.ok("커플 연결 완료");
  }

  @Operation(summary = "커플 연동 삭제")
  @DeleteMapping("/delete")
  public ResponseEntity<String> deleteCouple(HttpServletRequest request) {
    coupleService.deleteCouple(request);
    return ResponseEntity.ok("커플 삭제 완료");
  }

}
