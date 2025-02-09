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
public class RedisViewCountSyncService {
    private static final String REVIEW_VIEW_COUNT_KEY_PREFIX="review:view:";
    private final RedisTemplate<String, String> redisTemplate;
    private final ReviewRepository reviewRepository;

    /**
     * 5분단위 Redis 저장
     */
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void syncViewCount() {
        Set<String> keys = redisTemplate.keys(REVIEW_VIEW_COUNT_KEY_PREFIX + "*");
        if(keys == null || keys.isEmpty()) {
            return;
        } //예외처리 필요할듯

        for (String key:keys){
            //review:view:123 -> reviewId=123을 가져옴
            Long reviewId=Long.parseLong(key.replace(REVIEW_VIEW_COUNT_KEY_PREFIX,""));
            Object value=redisTemplate.opsForValue().get(key);
            if(value == null ) continue;
            Long redisCount=(Long)value;

            reviewRepository.findById(reviewId).ifPresent(review -> {
                int updateCount=review.getViewCount() + redisCount.intValue();
                review.setViewCount(updateCount);
                reviewRepository.save(review);
            });
            redisTemplate.delete(key);
        }

    }
}
