package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import solo.project.dto.Comment.CommentRequestDto;

import java.util.List;

public interface Comment {
  // 댓글 생성
  void createComment(Long Review_id, CommentRequestDto CommentRequestDto, HttpServletRequest request);
  // 대댓글 생성
  void createReply(Long reviewId, Long comment_id, CommentRequestDto CommentRequestDto, HttpServletRequest request);
  // 댓글 조회
  List<Comment> getComments(Long review_id);
  // 대댓글 조회
  List<Comment> getReplies(Long comment_id);
  // 댓글 수정
  void updateComment(Long comment_id, CommentRequestDto CommentRequestDto, HttpServletRequest request);
  // 댓글 삭제
  void deleteComment(Long comment_id, HttpServletRequest request);

}
