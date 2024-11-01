package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import solo.project.dto.User.request.UserLoginRequestDto;
import solo.project.dto.User.response.UserLoginResponseDto;
import solo.project.dto.User.response.UserProfileResponseDto;
import solo.project.dto.User.request.UserSignUpRequestDto;

public interface UserService {
    UserLoginResponseDto login(UserLoginRequestDto requestDto, HttpServletResponse response);
    void signUp(UserSignUpRequestDto requestDto, HttpServletResponse response);
    boolean isNicknameDuplicated(String nickname);
    UserProfileResponseDto viewProfile(HttpServletRequest request);
    void logout(HttpServletRequest request);
}
