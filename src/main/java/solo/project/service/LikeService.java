package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;

public interface LikeService {
  // 좋아요 생성(좋아요 있으면 삭제)
  void addLike(Long reviewId, HttpServletRequest request) ;
  void removeLike(Long reviewId, HttpServletRequest request) ;
}
