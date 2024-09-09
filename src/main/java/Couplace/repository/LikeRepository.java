package Couplace.repository;

import Couplace.entity.article.Article;
import Couplace.entity.like.Like;
import Couplace.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    void deleteByUserAndArticle(User user, Article article);
    boolean existsByUserAndArticle(User user, Article article);
}
