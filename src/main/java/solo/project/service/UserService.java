package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import solo.project.dto.request.UserLoginRequestDto;
import solo.project.dto.response.UserLoginResponseDto;
import solo.project.dto.request.UserProfileResponseDto;
import solo.project.dto.response.UserSignUpRequestDto;

public interface UserService {
    UserLoginResponseDto login(UserLoginRequestDto requestDto, HttpServletResponse response);
    void signUp(UserSignUpRequestDto requestDto, HttpServletResponse response);
    boolean isNicknameDuplicated(String nickname);
    UserProfileResponseDto viewProfile(HttpServletRequest request);
    void logout(HttpServletRequest request);
}
