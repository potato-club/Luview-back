package solo.project.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.request.UserCancel;
import solo.project.dto.request.UserLoginRequestDto;
import solo.project.dto.response.UserProfileResponseDto;
import solo.project.dto.response.UserLoginResponseDto;
import solo.project.dto.request.UserSignUpRequestDto;
import solo.project.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "users")
@Tag(name="User Authorization", description = "유저 및 인증 API")
public class UserController {
    private final UserService userService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> userSignUp(@RequestBody UserSignUpRequestDto requestDto, HttpServletResponse response){
        userService.signUp(requestDto,response);
        return ResponseEntity.ok("회원가입이 완료되었습니다");
    }

    //로그인
    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody UserLoginRequestDto requestDto, HttpServletResponse response) {
        return userService.login(requestDto, response);
    }

    // 프로필 조회
    @GetMapping("/profile")
    public UserProfileResponseDto viewProfile(HttpServletRequest request) {
        return userService.viewProfile(request);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }


    // 닉네임 중복 확인
    @GetMapping("/nickname/duplicate")
    public ResponseEntity<?> isNicknameDuplicated(@RequestParam String nickname){
        boolean isDuplicated = userService.isNicknameDuplicated(nickname);
        return ResponseEntity.ok().body(isDuplicated);
    }

    // 회원 탈퇴
    @PutMapping("/withdrawal")
    public ResponseEntity<String> withdrawalMembership(HttpServletRequest request) {
        userService.withdrawalMembership(request);
        return ResponseEntity.ok("회원탈퇴가 완료 되었습니다.");
    }

    // 탈퇴 취소
    @PutMapping("/cancel")
    public ResponseEntity<String> cancelWithdrawal(@RequestBody UserCancel cancelDto) {
        userService.cancelWithdrawal(cancelDto.getEmail(), cancelDto.isAgreement());
        return ResponseEntity.ok("회원탈퇴가 취소 되었습니다.");
    }

    @GetMapping("/reissue")
    public ResponseEntity<String> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        userService.reissueToken(request, response);
        return ResponseEntity.ok("토큰 재발급이 완료되었습니다.");
    }

    @GetMapping("/check")
    public ResponseEntity<String> validateToken(){
        return ResponseEntity.ok("Access Token");
    }
}
