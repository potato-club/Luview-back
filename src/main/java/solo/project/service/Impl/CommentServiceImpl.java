package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.dto.Comment.CommentRequestDto;
import solo.project.service.Comment;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements Comment {
  @Override
  public void createComment(Long Review_id, CommentRequestDto CommentRequestDto, HttpServletRequest request) {

  }

  @Override
  public void createReply(Long reviewId, Long comment_id, CommentRequestDto CommentRequestDto, HttpServletRequest request) {

  }

  @Override
  public List<Comment> getComments(Long review_id) {
    return List.of();
  }

  @Override
  public List<Comment> getReplies(Long comment_id) {
    return List.of();
  }

  @Override
  public void updateComment(Long comment_id, CommentRequestDto CommentRequestDto, HttpServletRequest request) {

  }

  @Override
  public void deleteComment(Long comment_id, HttpServletRequest request) {

  }
}
