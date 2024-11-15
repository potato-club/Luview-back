package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import solo.project.dto.kakao.UserKakaoResponseDto;
import solo.project.dto.User.request.UserLoginRequestDto;
import solo.project.dto.User.response.UserLoginResponseDto;
import solo.project.dto.User.response.UserProfileResponseDto;
import solo.project.dto.User.request.UserSignUpRequestDto;
import solo.project.entity.User;

@Service
public interface UserService {
    UserKakaoResponseDto kakaoLogin(String code,  HttpServletRequest request, HttpServletResponse response);
    UserLoginResponseDto login(UserLoginRequestDto requestDto, HttpServletResponse response);
    void signUp(UserSignUpRequestDto requestDto, HttpServletResponse response);
    boolean isNicknameDuplicated(String nickname);
    UserProfileResponseDto viewProfile(HttpServletRequest request);
    void logout(HttpServletRequest request);
    User findUserByToken(HttpServletRequest request);
    void reissueToken(HttpServletRequest request, HttpServletResponse response);
    void withdrawalMembership(HttpServletRequest request);
    void cancelWithdrawal(String email, boolean agreement);
}
