package Couplace.service;

import Couplace.dto.LikeResponse;
import Couplace.entity.article.Article;
import Couplace.entity.like.Like;
import Couplace.entity.user.User;
import Couplace.exception.AppException;
import Couplace.exception.ErrorCodes;
import Couplace.repository.BlogRepository;
import Couplace.repository.LikeRepository;
import Couplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final BlogRepository blogRepository; // Repository는 BlogRepository로 설정
    private final UserRepository userRepository;

    public LikeResponse likeArticle(Long articleId, String userEmail) { // 메서드명과 엔티티는 Article 유지
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCodes.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Article article = blogRepository.findById(articleId) // BlogRepository를 통해 Article을 조회
                .orElseThrow(() -> new AppException(ErrorCodes.ARTICLE_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        LikeResponse response = new LikeResponse();

        // 이미 좋아요를 눌렀는지 확인
        if (likeRepository.existsByUserAndArticle(user, article)) {
            likeRepository.deleteByUserAndArticle(user, article); // 좋아요 취소
            response.setMessage("좋아요 취소");
        } else {
            likeRepository.save(Like.builder().article(article).user(user).build()); // 좋아요 추가
            response.setMessage("좋아요 성공");
        }

        return response;
    }
}
