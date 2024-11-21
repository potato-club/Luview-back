package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.dto.Comment.CommentRequestDto;
import solo.project.dto.Comment.CommentResponseDto;
import solo.project.entity.Comment;
import solo.project.entity.Review;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.CommentRepository;
import solo.project.repository.ReviewRepository;
import solo.project.service.CommentService;
import solo.project.service.ReviewService;
import solo.project.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
  private final UserService userService;
  private final ReviewService reviewService;
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;


  @Override
  public void createComment(Long review_id, CommentRequestDto commentRequestDto, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if(user == null){
      throw new NotFoundException("로그인 후 댓글 작성 가능합니다.",ErrorCode.NOT_FOUND_EXCEPTION);
    }
    Review review = reviewService.getReview(review_id);
    if (review == null) {
      throw new NotFoundException("찾을 수 없는 리뷰글입니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }

    Comment comment = Comment.builder()
        .user(user)
        .review(review)
        .content(commentRequestDto.getContent())
        .build();

    commentRepository.save(comment);

  }

  @Override
  public void createReply(Long reviewId, Long comment_id, CommentRequestDto commentRequestDto, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if(user == null){
      throw new NotFoundException("로그인 후 댓글 작성 가능합니다.",ErrorCode.NOT_FOUND_EXCEPTION);
    }
    Review review = reviewService.getReview(reviewId);
    if (review == null) {
      throw new NotFoundException("찾을 수 없는 리뷰글입니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }
    Comment parent = commentRepository.findById(comment_id).orElse(null);
    if (parent == null) {
      throw new NotFoundException("찾을 수 없는 댓글입니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }
    if (parent.getParent() != null) {
      throw new NotFoundException("작성할 수 없는 댓글입니다", ErrorCode.NOT_FOUND_EXCEPTION);
    }

    Comment comment = Comment.builder()
        .user(user)
        .review(review)
        .parent(parent)
        .content(commentRequestDto.getContent())
        .build();

    commentRepository.save(comment);
  }

  @Override
  public List<CommentResponseDto> getComments(Long review_id) {
    Review review = reviewRepository.findById(review_id).orElse(null);
    if (review == null) {
      throw new NotFoundException("리뷰글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }

    List<Comment> comments = review.getComments();
    return comments.stream()
        .filter(comment -> comment.getParent() == null)
        .map(comment -> CommentResponseDto.builder()
            .id(comment.getId())
            .createdDate(comment.getCreatedDate())
            .content(comment.getContent())
            .children(getReplies(comment.getId()))
            .build())
        .collect(Collectors.toList());

  }

  @Override
  public List<CommentResponseDto> getReplies(Long comment_id) {
    Comment parent = commentRepository.findById(comment_id).orElse(null);
    if (parent == null) {
      throw new NotFoundException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }

    List<Comment> replies = parent.getChildren();
    return replies.stream()
        .map(reply -> CommentResponseDto.builder()
            .parent_id(reply.getParent().getId())
            .id(reply.getId())
            .createdDate(reply.getCreatedDate())
            .content(reply.getContent())
            .build())
        .collect(Collectors.toList());
  }

  @Override
  public void updateComment(Long comment_id, CommentRequestDto commentRequestDto, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if (user == null)
      throw new UnAuthorizedException("댓글 수정 권한이 없습니다", ErrorCode.UNAUTHORIZED_EXCEPTION);

    Comment comment = commentRepository.findById(comment_id).orElse(null);
    if (comment == null)
      throw new NotFoundException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    if (comment.getUser() != user)
      throw new UnAuthorizedException("댓글 수정 권한이 없습니다", ErrorCode.UNAUTHORIZED_EXCEPTION);

    comment.update(commentRequestDto);
  }

  @Override
  public void deleteComment(Long comment_id, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if (user == null)
      throw new UnAuthorizedException("댓글 삭제 권한이 없습니다", ErrorCode.UNAUTHORIZED_EXCEPTION);

    Comment comment = commentRepository.findById(comment_id).orElse(null);
    if (comment == null)
      throw new NotFoundException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    if (comment.getUser() != user)
      throw new UnAuthorizedException("댓글 삭제 권한이 없습니다", ErrorCode.UNAUTHORIZED_EXCEPTION);

    Review review = comment.getReview();
    review.downCommentCount();
    commentRepository.delete(comment);
  }
}
