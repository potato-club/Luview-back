package solo.project.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solo.project.dto.kakao.AdditionalInfoRequest;
import solo.project.dto.kakao.response.UserKakaoResponseDto;
import solo.project.error.exception.ForbiddenException;
import solo.project.kakao.KakaoApi;
import solo.project.dto.jwt.JwtTokenProvider;
import solo.project.dto.user.request.UserLoginRequestDto;
import solo.project.dto.user.response.UserProfileResponseDto;
import solo.project.dto.user.response.UserLoginResponseDto;
import solo.project.dto.user.request.UserSignUpRequestDto;
import solo.project.entity.User;
import solo.project.enums.LoginType;
import solo.project.error.ErrorCode;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;
import solo.project.repository.file.FileRepository;
import solo.project.repository.UserRepository;
import solo.project.service.redis.RedisEmailAuthentication;
import solo.project.service.redis.RedisJwtService;
import solo.project.service.UserService;

import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisJwtService redisJwtService;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoApi kakaoApi;
    private final FileRepository fileRepository;
    private final RedisEmailAuthentication redisEmailAuthentication;

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
            User existingUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("401, 이메일을 찾을 수 없습니다",ErrorCode.NOT_FOUND_EMAIL_EXCEPTION));

            if(existingUser.hasAdditionalInfo()){
                this.setJwtTokenInHeader(email,response);
                return UserKakaoResponseDto.builder()
                        .id(existingUser.getId()) //이메일로 조회 후 아이디 값 찾아서 반환
                        .email(email)
                        .nickname(nickname)
                        .responseCode("200, 로그인 되었습니다.")
                        .build();
            }

            return UserKakaoResponseDto.builder()
                    .id(existingUser.getId())
                    .email(email)
                    .nickname(nickname)
                    .responseCode("201, 추가 정보를 아직 입력하지않았습니다.")
                    .build();

        }
        //탈퇴한 회원인지 확인후에 탈퇴취소 회원인경우에는 다시 회원가입
        if(userRepository.existsByEmailAndDeletedIsTrue(email)){
            User user=userRepository.findByEmail(email).orElseThrow(()->new NotFoundException("401, 이메일을 찾을 수 없습니다",ErrorCode.NOT_FOUND_EMAIL_EXCEPTION));
            user.setDeleted(false);
            userRepository.save(user); //탈퇴 취소 고객은 취소 후 변경 사항 저장
            this.setJwtTokenInHeader(email,response);

            return UserKakaoResponseDto.builder()
                    .id(user.getId())
                    .email(email)
                    .nickname(nickname)
                    .responseCode("2000")
                    .build();
        }

        User newUser = User.builder()
                .email(email)
                .nickname(nickname)
                .loginType(LoginType.KAKAO) // 카카오 로그인 타입으로 설정
                .build();

        userRepository.save(newUser); //회원이 아닐경우 신규 회원으로 입력 받고 레포에 저장 그리고 리턴

        return UserKakaoResponseDto.builder()
                .id(newUser.getId())
                .email(email)
                .nickname(nickname)
                .responseCode("201, 회원가입 후 추가 정보를 입력해주세요.")
                .build();
    }

    @Transactional
    public User updateAdditionalInfo(Long id , AdditionalInfoRequest request){
        User user=userRepository.findById(id).orElseThrow(()->new NotFoundException("사용자를 찾을 수 없습니다",ErrorCode.NOT_FOUND_EXCEPTION));
        user.setName(request.getName());
        user.setBirthDate(request.getBirthDate());

        return userRepository.save(user);
    }

    //이메일 , 탈퇴 회원
    @Override
    public UserLoginResponseDto login(UserLoginRequestDto requestDto, HttpServletResponse response) {
        if (!userRepository.existsByEmail(requestDto.getEmail())) {
            throw new NotFoundException("2001, 회원이 아닙니다",ErrorCode.NOT_FOUND_EXCEPTION); //404
        }
        if (userRepository.existsByEmailAndDeletedIsTrue(requestDto.getEmail())) {
            throw  new ForbiddenException("2002, 탈퇴계정입니다.", ErrorCode.FORBIDDEN_EXCEPTION); //403
        }
        User user = findByEmailOrThrow(requestDto.getEmail());
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new UnAuthorizedException("401, 패스워드 불일치", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
        this.setJwtTokenInHeader(requestDto.getEmail(), response);
        return UserLoginResponseDto.builder()
                .responseCode("로그인 되었습니다.")
                .build();
    }



    @Override
    @Transactional
    public void signUp(UserSignUpRequestDto requestDto, HttpServletResponse response) {
        if(requestDto.getName() == null || requestDto.getBirthDate() == null){
            throw new IllegalArgumentException("로컬 가입시 이름과 생년월일이 필수입니다.");
        }

        if(userRepository.existsByEmail(requestDto.getEmail())) {
            throw new UnAuthorizedException("401", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }//이메일 존재 여부 확인

        if (!redisEmailAuthentication.isEmailVerified(requestDto.getEmail())) {
            throw new UnAuthorizedException("이메일 인증이 완료되지 않았습니다.",ErrorCode.NOT_VALID_EMAIL_EXCEPTION);
        }

        if (requestDto.getLoginType().equals(LoginType.NORMAL)) { // 로컬은 2차 인증 후 토큰 발급
            requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));

            User user = requestDto.toEntity();
            user.setEmailOtp(true);

            userRepository.save(user);

            redisEmailAuthentication.deleteEmailOtpData("verified:" + requestDto.getEmail());
        } else {
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
        findUserByToken(request);
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
        User user =this.findUserByToken(request);
        return fileRepository.getUserProfile(user);
    }

    //토큰 발급
    public void setJwtTokenInHeader(String email, HttpServletResponse response){
        Optional<User> user = userRepository.findByEmail(email);

        if(user.isEmpty()){
            throw new UnAuthorizedException("NOT FOUND USER", ErrorCode.ACCESS_DENIED_EXCEPTION);
        } //유저를 찾을수 없을 때

        String accessToken= jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        jwtTokenProvider.setHeaderAccessToken(response,accessToken);
        jwtTokenProvider.setHeaderRefreshToken(response,refreshToken);

        redisJwtService.setValues(refreshToken,email);
    }
}
