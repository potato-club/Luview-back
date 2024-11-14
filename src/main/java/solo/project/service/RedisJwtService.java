package solo.project.service;

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

    //Email에 대한 리프레쉬 기간 설정 (7일)
    public void setValues(String token, String email){
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        Map<String,String> map= new HashMap<>();
        map.put("email",email);
        operations.set(token,map, Duration.ofDays(7));
    }

    //Key값으로 Value값 가져오기
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

    //key 통해서 이메일 OTP 리턴을 합니다.
    public Object getEmailOtpData(String key){
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get(key);
        if(value ==null){
            throw new NotFoundException("Email OTP not found for key!!" + key, ErrorCode.NOT_FOUND_EXCEPTION);
        }
        return value;
    }

    //유효시간 Email Otp
    public void setEmailOtpData(String key, String value, long duration){
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Duration exprireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key,value,exprireDuration);
    }

    //email에 대한 otp값 삭제
    public void deleteEmailOtpData(String key){
        redisTemplate.delete(key);
    }

    //기존 Otp가 있다면 삭제
    public void deleteExistingOtp(String email){
        if(Boolean.TRUE.equals(redisTemplate.hasKey(email))){
            redisTemplate.delete(email);
        }
    }
}
