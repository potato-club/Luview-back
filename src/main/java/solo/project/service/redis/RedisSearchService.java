package solo.project.service.redis;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import solo.project.entity.User;
import solo.project.service.UserService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisSearchService {
    //최근검색어 서치에 대한 코드임
    private static final String KEY_PREFIX = "user:recentSearch:";
    //이용자들의 최근 검색어를 토대로 급상승 검색어에 대한 코드
    private static final String SEARCH_KEY_PREFIX = "search:trending:";

    private static final int MAX_SEARCH_TERMS = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;

    public void addSearchTerm(String userId, String term) {
        String key = KEY_PREFIX + userId;
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        double score = Instant.now().toEpochMilli();
        zSetOperations.add(key, term, score);

        Long currentSize = zSetOperations.size(key);
        if (currentSize != null && currentSize > MAX_SEARCH_TERMS) {
            zSetOperations.removeRange(key, 0, currentSize - MAX_SEARCH_TERMS - 1);
        }

        //사용자들의 검색키를 기반으로 트렌딩으로 누적,
        ZSetOperations<String, String> trendingZSet = redisTemplate.opsForZSet();
        trendingZSet.incrementScore(SEARCH_KEY_PREFIX, term, 1);
    }

    public List<String> getRecentSearchTerms(HttpServletRequest request) {
        User user = userService.findUserByToken(request);
        String key = KEY_PREFIX + user.getId();
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<String> terms = zSetOperations.reverseRange(key, 0, MAX_SEARCH_TERMS - 1);
        return (terms == null) ? new ArrayList<>() : new ArrayList<>(terms);
    }

    public String clearRecentSearchTerms(HttpServletRequest request) {
        User user = userService.findUserByToken(request);
        String key = KEY_PREFIX + user.getId().toString();
        redisTemplate.delete(key);
        return "최근 검색어 목록이 삭제되었습니다.";
    }

    public List<String> getTrendingSearchTerms(HttpServletRequest request) {
        User user = userService.findUserByToken(request);
        ZSetOperations<String, String> trendingZSet = redisTemplate.opsForZSet();
        Set<String> trending = trendingZSet.reverseRange(SEARCH_KEY_PREFIX, 0, MAX_SEARCH_TERMS - 1);
        return trending == null ? new ArrayList<>() : new ArrayList<>(trending);
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void resetTrendingSearchTerms() {
        redisTemplate.delete(SEARCH_KEY_PREFIX);
    }
}