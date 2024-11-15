package solo.project.kakao;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import solo.project.error.ErrorCode;
import solo.project.error.exception.UnAuthorizedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
@Service
@RequiredArgsConstructor
public class KakaoApi {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoLocalRedirectUri;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    private final RestTemplate restTemplate;
    private static final String reqAccessTokenURL = "https://kauth.kakao.com/oauth/token";
    private static final String reqUserInfoURL = "https://kapi.kakao.com/v2/user/me";

    private static final Logger logger = LoggerFactory.getLogger(KakaoApi.class);

    public String getAccessToken(String authorize_code, HttpServletRequest request) {
        String access_Token;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String redirectUri = this.selectRedirectUri(request);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "authorization_code");
        parameters.add("client_id", kakaoClientId);
        parameters.add("client_secret", kakaoClientSecret);
        parameters.add("redirect_uri", redirectUri);
        parameters.add("code", authorize_code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(reqAccessTokenURL, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(responseEntity.getBody())).getAsJsonObject();
            access_Token = jsonObject.get("access_token").getAsString();
        } else {
            // Log the response for debugging
            String errorMessage = responseEntity.getBody();
            Logger logger = LoggerFactory.getLogger(KakaoApi.class);
            logger.error("Failed to get access token. Response: " + errorMessage);
            throw new UnAuthorizedException("Failed to get access token!", ErrorCode.KAKAO_ACCESS_TOKEN_FAILED);
        }

        return access_Token;
    }

    public Map<String, String> getUserInfo(String accessToken) {
        String email;
        String nickname;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(reqUserInfoURL, HttpMethod.GET, request, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(responseEntity.getBody())).getAsJsonObject();
            JsonObject kakaoAccount = jsonObject.getAsJsonObject("kakao_account");
            email = kakaoAccount.getAsJsonObject().get("email").getAsString();
            JsonObject profile = kakaoAccount.getAsJsonObject("profile");
            nickname = profile.get("nickname").getAsString();
        } else {
            throw new UnAuthorizedException("Failed to get user info!", ErrorCode.KAKAO_USER_INFO_FAILED);
        }

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", email);
        userInfo.put("nickname", nickname);
        return userInfo;
    }

    private String selectRedirectUri(HttpServletRequest request) {
        String originHeader = request.getHeader("authorization");

        if (originHeader != null && originHeader.contains("Bearer")) {
            return kakaoLocalRedirectUri; // authorization 헤더가 있는 경우
        } else {
            return kakaoLocalRedirectUri; // authorization 헤더가 없는 경우
        }
    }

}



//@Service
//@RequiredArgsConstructor
//public class KakaoApi {
//
//    @Value("${spring.security.oauth2.client.registration.kakao-local.client-id}")
//    private String kakaoClientId;
//
//    @Value("${spring.security.oauth2.client.registration.kakao-local.redirect-uri}")
//    private String kakaoLocalRedirectUri;
//
//    private final RestTemplate restTemplate;
//    private static final String reqAccessTokenURL = "https://kauth.kakao.com/oauth/token";
//    private static final String reqUserInfoURL = "https://kapi.kakao.com/v2/user/me";
//
//    public String getAccessToken(String authorize_code, HttpServletRequest request) {
//        String access_Token;
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        String redirectUri = this.selectRedirectUri(request);
//
//        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
//        parameters.add("grant_type", "authorization_code");
//        parameters.add("client_id", kakaoClientId);
//        parameters.add("redirect_uri", redirectUri);
//        parameters.add("code", authorize_code);
//
//        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers);
//
//        ResponseEntity<String> responseEntity = restTemplate.postForEntity(reqAccessTokenURL, requestEntity, String.class);
//
//        if (responseEntity.getStatusCode() == HttpStatus.OK) {
//            JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(responseEntity.getBody())).getAsJsonObject();
//            access_Token = jsonObject.get("access_token").getAsString();
//        } else {
//            throw new UnAuthorizedException("Failed to get access token!", ErrorCode.KAKAO_ACCESS_TOKEN_FAILED);
//        }
//
//        return access_Token;
//    }
//
//    public String getUserInfo(String accessToken) {
//        String email;
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//
//        HttpEntity request = new HttpEntity(headers);
//
//        ResponseEntity<String> responseEntity = restTemplate.exchange(reqUserInfoURL, HttpMethod.GET, request, String.class);
//
//        if (responseEntity.getStatusCode() == HttpStatus.OK) {
//            JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(responseEntity.getBody())).getAsJsonObject();
//            JsonObject kakaoAccount = jsonObject.getAsJsonObject("kakao_account");
//            email = kakaoAccount.getAsJsonObject().get("email").getAsString();
//        } else {
//            throw new UnAuthorizedException("Failed to get user info!", ErrorCode.KAKAO_USER_INFO_FAILED);
//        }
//
//        return email;
//    }
//
//    private String selectRedirectUri(HttpServletRequest request) {
//        // origin 또는 referer 헤더를 확인
//        String originHeader = request.getHeader("origin");
//        String refererHeader = request.getHeader("referer");
//
//        // origin 또는 referer가 localhost를 포함하는지 확인
//        if ((originHeader != null && originHeader.contains("localhost")) ||
//                (refererHeader != null && refererHeader.contains("localhost"))) {
//            return "http://localhost:8080/login/oauth2/code/kakao";
//        }
//
//        // origin이나 referer가 null일 경우 ServerName을 체크하여 localhost인지 확인
//        if ("localhost".equals(request.getServerName())) {
//            return "http://localhost:8080/login/oauth2/code/kakao";
//        }
//
//        // 외부 URI라면 kakaoLocalRedirectUri 반환
//        return kakaoLocalRedirectUri;
//    }
//
//}
