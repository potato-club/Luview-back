package solo.project.service.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisReviewService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String POPULAR_REVIEW_KEY="popularReviews";

    //인기 게시물 캐싱
    public void setPopularReview(Object popularReviews, Duration duration) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(POPULAR_REVIEW_KEY, popularReviews, duration);
    }

    //캐싱 된 인기 게시물을 가져옴
    public Object getPopularReviews(){
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        return operations.get(POPULAR_REVIEW_KEY);
    }

    public Object deletePopularReviews() {
        return redisTemplate.delete(POPULAR_REVIEW_KEY);
    }

    public RedisReviewService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
