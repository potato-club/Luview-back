package solo.project.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.request.UserLoginRequestDto;
import solo.project.dto.request.UserProfileResponseDto;
import solo.project.dto.response.UserLoginResponseDto;
import solo.project.dto.response.UserSignUpRequestDto;
import solo.project.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "users")
@Tag(name="User Authorization", description = "유저 및 인증 API")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody UserSignUpRequestDto requestDto, HttpServletResponse response) {
        userService.signUp(requestDto, response);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@RequestBody UserLoginRequestDto requestDto, HttpServletResponse response) {
        UserLoginResponseDto loginResponse = userService.login(requestDto, response);
        return ResponseEntity.ok(loginResponse); // 200 OK
    }

    // 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> viewProfile(HttpServletRequest request) {
        UserProfileResponseDto profile = userService.viewProfile(request);
        return ResponseEntity.ok(profile); // 200 OK
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // 닉네임 중복 확인
    @GetMapping("/nickname/duplicate")
    public ResponseEntity<Boolean> isNicknameDuplicated(@RequestParam String nickname) {
        boolean isDuplicated = userService.isNicknameDuplicated(nickname);
        return ResponseEntity.ok(isDuplicated); // 200 OK
    }

    // 회원 탈퇴
    @DeleteMapping("/withdrawal")
    public ResponseEntity<Void> withdrawalMembership(HttpServletRequest request) {
        userService.withdrawalMembership(request);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // 탈퇴 취소
    @PostMapping("/cancel-withdrawal")
    public ResponseEntity<Void> cancelWithdrawal(@RequestParam String email, @RequestParam boolean agreement) {
        userService.cancelWithdrawal(email, agreement);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
