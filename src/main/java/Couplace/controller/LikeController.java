package Couplace.controller;

import Couplace.dto.LikeResponse;
import Couplace.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/{articleId}/like")
    public ResponseEntity<LikeResponse> likeArticle(@PathVariable Long articleId, Authentication authentication) {
        String userEmail = authentication.getName(); // SecurityContext에서 사용자 이메일 가져오기
        LikeResponse response = likeService.likeArticle(articleId, userEmail);
        return ResponseEntity.ok(response);
    }
}

