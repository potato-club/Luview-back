package solo.project.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.repository.review.ReviewRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisCountSyncService {
    private static final String REVIEW_VIEW_COUNT_KEY_PREFIX="review:view:";
    private static final String REVIEW_LIKE_COUNT_KEY_PREFIX="review:like:";
    private final RedisTemplate<String, String> redisTemplate;
    private final ReviewRepository reviewRepository;

    //스케줄 5분 설정해둠 대부분 다 5분
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void syncViewCount() {
        Set<String> keys = redisTemplate.keys(REVIEW_VIEW_COUNT_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            // key 예: "review:view:123" 에서 reviewId 추출
            Long reviewId = Long.parseLong(key.replace(REVIEW_VIEW_COUNT_KEY_PREFIX, ""));
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) continue;

            Long redisCount;
            try {
                redisCount = Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                continue;
            }

            reviewRepository.findById(reviewId).ifPresent(review -> {
                int updateCount = review.getViewCount() + redisCount.intValue();
                review.setViewCount(updateCount);
                reviewRepository.save(review);
            });
            redisTemplate.delete(key);
        }
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void syncLikeCount() {
        Set<String> likeKeys=redisTemplate.keys(REVIEW_LIKE_COUNT_KEY_PREFIX + "*");
        if (likeKeys == null || likeKeys.isEmpty()) {
            return;
        }
        for(String likeKey : likeKeys) {
            Long reviewId = Long.parseLong(likeKey.replace(REVIEW_LIKE_COUNT_KEY_PREFIX, ""));
            Object value = redisTemplate.opsForValue().get(likeKey);
            if (value == null) continue;
            Long redisCount;
            try{
                redisCount = Long.parseLong(value.toString());
            }catch (NumberFormatException e) {
                continue;
            }
            reviewRepository.findById(reviewId).ifPresent(review -> {
                int updateLikeCount = review.getLikeCount() + redisCount.intValue();
                review.setLikeCount(updateLikeCount);
                reviewRepository.save(review);
            });
            redisTemplate.delete(likeKey);
        }
    }

}
