package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.dto.User.request.UserLoginRequestDto;
import solo.project.dto.User.response.UserProfileResponseDto;
import solo.project.dto.User.response.UserLoginResponseDto;
import solo.project.dto.User.request.UserSignUpRequestDto;
import solo.project.entity.User;
import solo.project.enums.LoginType;
import solo.project.enums.UserRole;
import solo.project.error.ErrorCode;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //이메일 , 탈퇴 회원, 2차인증 확인 아닐경우 오류 코드 출력
    @Override
    public UserLoginResponseDto login(UserLoginRequestDto requestDto, HttpServletResponse response) {
        if(!userRepository.existsByEmailAndDeletedAndEmailOtp(requestDto.getEmail(),false, true)) {
            if (!userRepository.existsByEmail(requestDto.getEmail())) {
                return UserLoginResponseDto.builder()
                        .responseCode("2001") //회원이 아닐 경우 코드
                        .build();
            } else if (userRepository.existsByEmailAndDeletedIsTrue(requestDto.getEmail())) {
                return UserLoginResponseDto.builder()
                        .responseCode("2002") //탈퇴를 한 경우
                        .build();
            }else if (userRepository.existsByEmailAndDeletedIsFalse(requestDto.getEmail())) {
                userRepository.delete(userRepository.findByEmail(requestDto.getEmail()).orElseThrow());
                return UserLoginResponseDto.builder()
                        .responseCode("2003") //2차인증이 제대로 되지 않은 경우
                        .build();
            }
        }
        User user =userRepository.findByEmail(requestDto.getEmail()).orElseThrow();

        //패스워드가 일치하지 않을경우 에러코드 발생
        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new UnAuthorizedException("401", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        this.setJwtTokenInHeader(requestDto.getEmail(), response);

        return UserLoginResponseDto.builder()
                .responseCode("200")
                .build();
}

    @Override
    @Transactional
    public void signUp(UserSignUpRequestDto requestDto, HttpServletResponse response) {
        if(userRepository.existsByEmail(requestDto.getEmail())) {
            throw new UnAuthorizedException("401", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }//이메일 존재 여부 확인

        //카카오 쇼설을 넣어야합니다.

        if(requestDto.getLoginType().equals(LoginType.NOMAL)){ //로컬은 2차 인증 후 토큰 발급
            requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));

            User user = requestDto.toEntity();
            user.setEmailOtp(false);

            userRepository.save(user);
        }else {
            throw new UnAuthorizedException("401_NOT_ALLOW", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
    }

    //닉네임 중복 확인
    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    @Override
    public UserProfileResponseDto viewProfile(HttpServletRequest request) {
        return null;
    }

    @Override
    public void logout(HttpServletRequest request) {

    }

    public void setJwtTokenInHeader(String email, HttpServletResponse response){
        Optional<User> user = userRepository.findByEmail(email);

        if(user.isEmpty()){
            throw new UnAuthorizedException("NOT FOUND USER", ErrorCode.ACCESS_DENIED_EXCEPTION);
        } //유저를 찾을수 없을 때

        UserRole userRole = user.get().getUserRole();

    }

}
