package solo.project.service.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisReviewListService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String POPULAR_REVIEW_KEY="popularReviews";
    private static final String POPULAR_REVIEW_BY_LIKE_KEY="popularLikes";

    //인기 게시물 캐싱
    public void setPopularReview(Object popularReviews, Duration duration) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(POPULAR_REVIEW_KEY, popularReviews, duration);
    }

    //캐싱 된 인기 게시물을 가져옴 조회순임 default== 조회순
    public Object getPopularReviews(){
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        return operations.get(POPULAR_REVIEW_KEY);
    }

    //좋아요 기준 게시물을 가져옴
    public Object getPopularLikes(){
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        return operations.get(POPULAR_REVIEW_BY_LIKE_KEY);
    }

    public void setPopularReviewByLikeKey(Object reviews, Duration duration){
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(POPULAR_REVIEW_BY_LIKE_KEY, reviews, duration);
    }

    //인기게시물이기 때문에 삭제는 굳..이?
    public Object deletePopularReviews() {
        return redisTemplate.delete(POPULAR_REVIEW_KEY);
    }

    public RedisReviewListService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
