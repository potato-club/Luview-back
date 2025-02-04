package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solo.project.entity.Favorites;
import solo.project.entity.Review;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.FavoritesRepository;
import solo.project.repository.review.ReviewRepository;
import solo.project.service.FavoritesService;
import solo.project.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoritesServiceImpl implements FavoritesService {

  private final UserService userService;
  private final ReviewRepository reviewRepository;
  private final FavoritesRepository favoritesRepository;

  @Override
  public void createFavorite(Long review_id, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if (user == null) {
      throw new UnAuthorizedException("로그인 후 즐겨찾기 가능합니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
    }

    Review review = reviewRepository.findById(review_id)
        .orElseThrow(() -> new NotFoundException("찾을 수 없는 리뷰글 입니다.", ErrorCode.NOT_FOUND_EXCEPTION));

    if(favoritesRepository.existsByUserAndReview(user, review)) {
      throw new IllegalArgumentException("이미 즐겨찾기 했습니다.");
    }

    Favorites favorites =Favorites.builder()
        .user(user)
        .review(review)
        .build();

    favoritesRepository.save(favorites);
  }

  @Override
  public void deleteFavorite(Long review_id, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if (user == null) {
      throw new UnAuthorizedException("로그인 후 즐겨찾기 삭제 가능합니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
    }

    Review review = reviewRepository.findById(review_id)
        .orElseThrow(() -> new NotFoundException("찾을 수 없는 리뷰글 입니다.", ErrorCode.NOT_FOUND_EXCEPTION));

    Favorites favorite = favoritesRepository.findByUserAndReview(user, review)
        .orElseThrow(() -> new IllegalArgumentException("즐겨찾기 내역이 존재하지 않습니다."));

    favoritesRepository.delete(favorite);
  }

  @Override
  public List<Favorites> getFavoritesByUser(HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if (user == null) {
      throw new UnAuthorizedException("로그인 후 즐겨찾기 조회 가능합니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
    }
    return favoritesRepository.findByUser(user);
  }


}
