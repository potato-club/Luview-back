package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.entity.Couple;
import solo.project.entity.User;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.repository.CoupleRepository;
import solo.project.repository.UserRepository;
import solo.project.service.CoupleService;
import solo.project.service.UserService;

@Service
@Transactional
@RequiredArgsConstructor
public class CoupleServiceImpl implements CoupleService {

  private final UserRepository userRepository;
  private final CoupleRepository coupleRepository;
  private final UserService userService;

  @Override
  public void createCouple(String partnerCode, HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if (user == null) {
      throw new NotFoundException("로그인 후 커플 연동 가능합니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }
    User partner  = userRepository.findByUniqueCode(partnerCode).
        orElseThrow(() -> new IllegalArgumentException("유효하지 않는 코드입니다."));

    if (user.equals(partner)) {
      throw new IllegalArgumentException("자신과 연결할 수 없습니다.");
    }
    // 이미 커플인지 확인
    if (coupleRepository.existsByUser1AndUser2(user, partner) ||
        coupleRepository.existsByUser1AndUser2(partner, user)) {
      throw new IllegalStateException("이미 커플로 연결되어 있습니다.");
    }
    Couple couple = Couple.builder()
        .user1(user)
        .user2(partner)
        .build();

    coupleRepository.save(couple);
  }

  @Override
  public void deleteCouple(HttpServletRequest request) {
    User user = userService.findUserByToken(request);
    if (user == null) {
      throw new NotFoundException("로그인 후 커플 삭제 가능합니다.", ErrorCode.NOT_FOUND_EXCEPTION);
    }

    Couple couple = coupleRepository.findByUser1OrUser2(user,user)
        .orElseThrow(() -> new NotFoundException("사용자가 속한 커플이 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

    coupleRepository.delete(couple);
  }
}
