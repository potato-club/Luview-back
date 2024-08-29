package Couplace.repository;


import Couplace.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Article, Long> {
}