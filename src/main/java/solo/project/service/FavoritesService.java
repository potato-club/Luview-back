package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import solo.project.entity.Favorites;

import java.util.List;

public interface FavoritesService {
  // 즐겨찾기 추가
  void createFavorite(Long review_id, HttpServletRequest request);
  // 즐겨찾기 삭제
  void deleteFavorite(Long review_id, HttpServletRequest request);
  // 사용자별 즐겨찾기 조회
  List<Favorites> getFavoritesByUser(HttpServletRequest request);
}
