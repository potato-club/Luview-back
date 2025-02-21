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
            throw new NotFoundException("이메일 OTP코드가 맞지않습니다! " + key, ErrorCode.NOT_FOUND_EXCEPTION);
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

    public void setEmailVerified(String email, boolean verified, long duration) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 인증 플래그를 저장할 key 예: "verified:사용자이메일"
        String verifiedKey = "verified:" + email;
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(verifiedKey, verified, expireDuration);
    }

    // 회원가입 시에 인증 여부를 확인
    public boolean isEmailVerified(String email) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get("verified:" + email);
        return Boolean.TRUE.equals(value);
    }

    public void setPasswordResetToken(String email, String code, long durationSeconds) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String key = "password-reset:" + code;
        Duration expireDuration = Duration.ofSeconds(durationSeconds);
        valueOperations.set(key, email, expireDuration);
    }

    // 비밀번호 재설정 토큰 조회
    public String getPasswordResetToken(String code) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String key = "password-reset:" + code;
        Object value = valueOperations.get(key);
        if (value == null) {
            throw new NotFoundException("비밀번호 재설정 토큰이 존재하지 않습니다: " , ErrorCode.NOT_FOUND_EXCEPTION);
        }
        return (String) value;
    }

    // 비밀번호 재설정 토큰 삭제 (재사용 방지)
    public void deletePasswordResetToken(String code) {
        String key = "password-reset:" + code;
        redisTemplate.delete(key);
    }
}
