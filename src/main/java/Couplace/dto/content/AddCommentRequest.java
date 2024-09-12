package Couplace.dto.content;

import Couplace.entity.article.Article;
import Couplace.entity.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddCommentRequest {
    private Long articleId;
    private String content;

    public Comment toEntity(String author, Article article)
    {
        return Comment.builder()
                .article(article)
                .author(author)
                .content(content)
                .build();
    }
}
