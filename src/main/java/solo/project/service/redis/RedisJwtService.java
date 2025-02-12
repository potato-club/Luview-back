package solo.project.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisJwtService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void setValues(String token, String email){
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        Map<String,String> map= new HashMap<>();
        map.put("email",email);
        operations.set(token,map, Duration.ofDays(7));
    }

    public Map<String,String> getValues(String token){
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        Object object = operations.get(token);
        if(object instanceof Map){
            return (Map<String,String>) object;
        }
        return null;
    }

    //Refresh Token 유효성 확인
    public boolean isRefreshTokenValid(String token){
        Map<String,String> values = getValues(token);
        return values !=null;
    }

    public void delValues(String token){
        redisTemplate.delete(token);
    }

}
