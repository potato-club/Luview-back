package solo.project.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisEmailAuthentication {
    private final RedisTemplate<String,Object> redisTemplate;

    public Object getEmailOtpData(String key) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get(key);
        if (value == null) {
            throw new NotFoundException("Email OTP not found for key: " + key, ErrorCode.NOT_FOUND_EXCEPTION);
        }
        return value;
    }

    // 유효 시간 동안 Email OTP(key, value) 저장
    public void setEmailOtpDataExpire(String key, String value, long duration) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    // Email OTP 값 삭제
    public void deleteEmailOtpData(String key) {
        redisTemplate.delete(key);
    }

    // 기존의 OTP 코드가 있는지 확인하고 있다면 삭제
    public void deleteExistingOtp(String email) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(email))) {
            redisTemplate.delete(email);
        }
    }
}
