package solo.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solo.project.dto.kakao.UserKakaoResponseDto;
import solo.project.dto.User.request.UserCancel;
import solo.project.dto.User.request.UserLoginRequestDto;
import solo.project.dto.User.response.UserProfileResponseDto;
import solo.project.dto.User.response.UserLoginResponseDto;
import solo.project.dto.User.request.UserSignUpRequestDto;
import solo.project.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/users")
@Tag(name="User Authorization", description = "유저 및 인증 API")
public class UserController {
    private final UserService userService;
    @Operation(summary = "카카오 로그인API")
    @GetMapping("/login/kakao")
    public UserKakaoResponseDto kakaoLogin(@RequestParam("code") String authorizeCode, HttpServletRequest request, HttpServletResponse response) {
        return userService.kakaoLogin(authorizeCode, request, response);
    }

    //회원가입
    @Operation(summary = "회원가입 API")
    @PostMapping("/signup")
    public ResponseEntity<String> userSignUp(@RequestBody UserSignUpRequestDto requestDto, HttpServletResponse response){
        userService.signUp(requestDto,response);
        return ResponseEntity.ok("회원가입이 완료되었습니다");
    }

    //로그인
    @Operation(summary = "일반 로그인 API")
    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody UserLoginRequestDto requestDto, HttpServletResponse response) {
        return userService.login(requestDto, response);
    }

    // 프로필 조회 아직 구현 덜했삼
    @Operation(summary = "내 정보 확인 API")
    @GetMapping("/profile")
    public UserProfileResponseDto viewProfile(HttpServletRequest request) {
        return userService.viewProfile(request);
    }

    // 로그아웃
    @Operation(summary = "로그아웃 API")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }


    // 닉네임 중복 확인
    @Operation(summary = "닉네임 중복 확인 API")
    @GetMapping("/nickname/duplicate")
    public ResponseEntity<?> isNicknameDuplicated(@RequestParam String nickname){
        boolean isDuplicated = userService.isNicknameDuplicated(nickname);
        return ResponseEntity.ok().body(isDuplicated);
    }

    // 회원 탈퇴
    @Operation(summary = "회원탈퇴 API")
    @PutMapping("/withdrawal")
    public ResponseEntity<String> withdrawalMembership(HttpServletRequest request) {
        userService.withdrawalMembership(request);
        return ResponseEntity.ok("회원탈퇴가 완료 되었습니다.");
    }

    // 탈퇴 취소
    @Operation(summary = "회원탈퇴 취소 API")
    @PutMapping("/cancel")
    public ResponseEntity<String> cancelWithdrawal(@RequestBody UserCancel cancelDto) {
        userService.cancelWithdrawal(cancelDto.getEmail(), cancelDto.isAgreement());
        return ResponseEntity.ok("회원탈퇴가 취소 되었습니다.");
    }

    @Operation(summary = "토큰 재발급 API")
    @GetMapping("/reissue")
    public ResponseEntity<String> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        userService.reissueToken(request, response);
        return ResponseEntity.ok("토큰 재발급이 완료되었습니다.");
    }

    @Operation(summary = "토큰 상태 확인 API")
    @GetMapping("/token_check")
    public ResponseEntity<String> validateToken(){
        return ResponseEntity.ok("Access Token");
    }
}
