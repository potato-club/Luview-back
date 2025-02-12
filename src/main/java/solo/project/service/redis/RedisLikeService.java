package solo.project.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisLikeService {
    //Redis로 받아서 멀쓰방
    private static final String REVIEW_LIKE_COUNT_KEY_PREFIX = "review:like:";
    private final RedisTemplate<String, Object> redisTemplate;

    public void incrementLikeCount(Long reviewId) {
        String incrkey = REVIEW_LIKE_COUNT_KEY_PREFIX + reviewId;
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.increment(incrkey, 1L);
    }

    public void decrementLikeCount(Long reviewId) {
        String decrkey = REVIEW_LIKE_COUNT_KEY_PREFIX + reviewId;
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.increment(decrkey, -1L);
    }
}