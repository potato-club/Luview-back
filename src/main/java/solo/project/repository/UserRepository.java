package solo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import solo.project.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // 이메일로 회원 찾기
    Optional<User> findByNickname(String nickname);
    boolean existsByEmail(String email);
    boolean existsByEmailAndDeleted(String email, boolean deleted);
    boolean existsByEmailAndDeletedAndEmailOtp(String email, boolean deleted, boolean otp);
    boolean existsByEmailAndDeletedIsTrue(String email);
    boolean existsByEmailAndDeletedIsFalse(String email);
}
