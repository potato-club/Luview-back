package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.Comment.CommentRequestDto;
import solo.project.dto.Comment.CommentResponseDto;
import solo.project.entity.Comment;
import solo.project.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/commnet")
@Tag(name="Commnet Controller", description = "댓글 API")
public class CommentController {
  private final CommentService commentService;

  @Operation(summary = "댓글 생성")
  @PostMapping("/{review_id}")
  public ResponseEntity<String> createComment(@PathVariable Long review_id,
                                              @RequestBody CommentRequestDto commentRequestDto,
                                              HttpServletRequest request) {
    commentService.createComment(review_id, commentRequestDto, request);
    return ResponseEntity.ok("댓글 작성 완료");
  }

  @Operation(summary = "대댓글 생성")
  @PostMapping("/{review_id}/{comment_id}")
  public ResponseEntity<String> createReply(@PathVariable Long review_id,
                                            @PathVariable Long comment_id,
                                            @RequestBody CommentRequestDto commentRequestDto,
                                            HttpServletRequest request) {
    commentService.createReply(review_id, comment_id, commentRequestDto, request);
    return ResponseEntity.ok("대댓글 작성 완료");
  }

  @Operation(summary = "댓글 조회")
  @GetMapping("/{review_id}")
  public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long review_id) {
    List<CommentResponseDto> comments = commentService.getComments(review_id);
    return ResponseEntity.ok(comments);
  }

  @Operation(summary = "댓글 수정")
  @PutMapping("/{comment_id}")
  public ResponseEntity<String> updateComment(@PathVariable Long comment_id,
                                                              @RequestBody CommentRequestDto commentRequestDto,
                                                              HttpServletRequest request) {
    commentService.updateComment(comment_id, commentRequestDto, request);
    return ResponseEntity.ok("댓글 수정 완료");
  }

  @Operation(summary = "댓글 삭제")
  @DeleteMapping("/{comment_id}")
  public ResponseEntity<String> deleteComment(@PathVariable Long comment_id, HttpServletRequest request){
    commentService.deleteComment(comment_id, request);
    return ResponseEntity.ok("댓글 삭제 완료");
  }

}
