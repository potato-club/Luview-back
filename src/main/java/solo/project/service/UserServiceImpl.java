package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.dto.kakao.UserKakaoResponseDto;
import solo.project.kakao.KakaoApi;
import solo.project.dto.jwt.JwtTokenProvider;
import solo.project.dto.User.request.UserLoginRequestDto;
import solo.project.dto.User.response.UserProfileResponseDto;
import solo.project.dto.User.response.UserLoginResponseDto;
import solo.project.dto.User.request.UserSignUpRequestDto;
import solo.project.entity.User;
import solo.project.enums.LoginType;
import solo.project.enums.UserRole;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisJwtService redisJwtService;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoApi kakaoApi;

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));
    }

    @Override
    public UserKakaoResponseDto kakaoLogin(String code,  HttpServletRequest request, HttpServletResponse response){
        String access_token= kakaoApi.getAccessToken(code, request);
        Map<String, String> userInfo = kakaoApi.getUserInfo(access_token);
        String email = userInfo.get("email");
        String nickname = userInfo.get("nickname");

        //토큰으로 상대방의 이메일정보 확인
        if(userRepository.existsByEmailAndDeleted(email,false)){
            this.setJwtTokenInHeader(email,response);

            return UserKakaoResponseDto.builder()
                    .responseCode("200, 로그인 되었습니다.")
                    .build();
        }
        //탈퇴한 회원인지 확인후에 탈퇴취소 회원인경우에는 다시 회원가입
        if(userRepository.existsByEmailAndDeletedIsTrue(email)){
            User user=userRepository.findByEmail(email).orElseThrow();
            user.setDeleted(false);
            this.setJwtTokenInHeader(email,response);

            return UserKakaoResponseDto.builder()
                    .responseCode("2000")
                    .build();
        }

        UserSignUpRequestDto requestDto = UserSignUpRequestDto.builder()
                .email(email)
                .nickname(nickname)
                .loginType(LoginType.KAKAO) // 카카오 로그인 타입으로 설정
                .build();

        // signUp 메서드를 호출하여 회원가입 처리
        signUp(requestDto, response);

        return UserKakaoResponseDto.builder()
                .email(email)
                .nickname(nickname)
                .responseCode("201, 회원가입 후 로그인 되었습니다.")
                .build();
    }

    //이메일 , 탈퇴 회원

    @Override
    public UserLoginResponseDto login(UserLoginRequestDto requestDto, HttpServletResponse response) {
        // 회원이 아닌 경우
        if (!userRepository.existsByEmail(requestDto.getEmail())) {
            return UserLoginResponseDto.builder()
                    .responseCode("2001, 회원이 아닙니다.") // 회원이 아닐 경우 코드
                    .build();
        }
        // 탈퇴한 회원인 경우
        if (userRepository.existsByEmailAndDeletedIsTrue(requestDto.getEmail())) {
            return UserLoginResponseDto.builder()
                    .responseCode("2002, 탈퇴하셨습니다.") // 탈퇴한 경우
                    .build();
        }
        // 회원 정보 조회
        User user = findByEmailOrThrow(requestDto.getEmail());
        // 패스워드가 일치하지 않을 경우 에러 코드 발생
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new UnAuthorizedException("401, 패스워드 불일치", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
        this.setJwtTokenInHeader(requestDto.getEmail(), response);
        return UserLoginResponseDto.builder()
                .responseCode("로그인 되었습니다.")
                .build();
    }

//    @Override
//    public UserLoginResponseDto login(UserLoginRequestDto requestDto, HttpServletResponse response) {
//        if(!userRepository.existsByEmailAndDeletedAndEmailOtp(requestDto.getEmail(),false, true)) {
//            if (!userRepository.existsByEmail(requestDto.getEmail())) {
//                return UserLoginResponseDto.builder()
//                        .responseCode("2001") //회원이 아닐 경우 코드
//                        .build();
//            } else if (userRepository.existsByEmailAndDeletedIsTrue(requestDto.getEmail())) {
//                return UserLoginResponseDto.builder()
//                        .responseCode("2002") //탈퇴를 한 경우
//                        .build();
//            }else if (userRepository.existsByEmailAndDeletedIsFalse(requestDto.getEmail())) {
//                userRepository.delete(userRepository.findByEmail(requestDto.getEmail()).orElseThrow());
//                return UserLoginResponseDto.builder()
//                        .responseCode("2003") //2차인증이 제대로 되지 않은 경우
//                        .build();
//            }
//        }
//        User user =findByEmailOrThrow(requestDto.getEmail());
//
//        //패스워드가 일치하지 않을경우 에러코드 발생
//        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
//            throw new UnAuthorizedException("401", ErrorCode.ACCESS_DENIED_EXCEPTION);
//        }
//
//        this.setJwtTokenInHeader(requestDto.getEmail(), response);
//
//        return UserLoginResponseDto.builder()
//                .responseCode("200")
//                .build();
//}

    @Override
    @Transactional
    public void signUp(UserSignUpRequestDto requestDto, HttpServletResponse response) {
        if(userRepository.existsByEmail(requestDto.getEmail())) {
            throw new UnAuthorizedException("401", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }//이메일 존재 여부 확인

        if(requestDto.getLoginType().equals(LoginType.KAKAO)){
            User user =requestDto.toEntity();
//            user.setEmailOtp(true);

            userRepository.save(user);
            this.setJwtTokenInHeader(requestDto.getEmail(), response);
        }else if(requestDto.getLoginType().equals(LoginType.NORMAL)){ //로컬은 2차 인증 후 토큰 발급
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

    // Refresh Token과 Access Token을 각각 확인하고 삭제
    @Override
    public void logout(HttpServletRequest request) {
        Optional.ofNullable(jwtTokenProvider.resolveRefreshToken(request))
                .ifPresent(redisJwtService::delValues);

        Optional.ofNullable(jwtTokenProvider.resolveAccessToken(request))
                .ifPresent(redisJwtService::delValues);
    }

    //액세스 토큰으로 회원 찾기
    @SneakyThrows
    @Override
    public User findUserByToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        if (token == null) {
            throw new UnAuthorizedException("토큰이 존재하지 않습니다.", ErrorCode.INVALID_TOKEN_EXCEPTION);
        }

        String email = jwtTokenProvider.findUserEmailByToken(token);
        return findByEmailOrThrow(email);
    }


    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        jwtTokenProvider.validateRefreshToken(refreshToken);
        String newAccessToken= jwtTokenProvider.reissueAccessToken(refreshToken,response);
        jwtTokenProvider.setHeaderAccessToken(response,newAccessToken);
    }
    //사용자 이메일 액세스 생성,, 예외처리를 넣어야하나.. 흠..

    //회원 탈퇴 메서드
    @Override
    public void withdrawalMembership(HttpServletRequest request) {
        User user= findUserByToken(request);
        user.setDeleted(true);
        this.logout(request);
    }

    //탈퇴 취소 메서드
    @Override
    @Transactional
    public void cancelWithdrawal(String email, boolean agreement) {
        if (userRepository.existsByEmailAndDeleted(email, true) && agreement) {
            User user = findByEmailOrThrow(email);
            user.setDeleted(false);
            userRepository.save(user);
        }else {
            throw new UnAuthorizedException("401_NOT_ALLOW", ErrorCode.NOT_ALLOW_WRITE_EXCEPTION);
        }
    }

    //유저 정보 추후 추가
    @Override
    public UserProfileResponseDto viewProfile(HttpServletRequest request) {
        return null;
    }

    //토큰 발급
    public void setJwtTokenInHeader(String email, HttpServletResponse response){
        Optional<User> user = userRepository.findByEmail(email);

        if(user.isEmpty()){
            throw new UnAuthorizedException("NOT FOUND USER", ErrorCode.ACCESS_DENIED_EXCEPTION);
        } //유저를 찾을수 없을 때

        UserRole userRole = user.get().getUserRole();

        String accessToken= jwtTokenProvider.createAccessToken(email,userRole);
        String refreshToken = jwtTokenProvider.createRefreshToken(email,userRole);

        jwtTokenProvider.setHeaderAccessToken(response,accessToken);
        jwtTokenProvider.setHeaderRefreshToken(response,refreshToken);

        redisJwtService.setValues(refreshToken,email);
    }
}
