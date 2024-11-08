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

@Service
@RequiredArgsConstructor
public class KakaoApi {

    private static final Logger logger = LoggerFactory.getLogger(KakaoApi.class);

    private final RestTemplate restTemplate;

    //나중에 도메인 추가

    @Value("${spring.security.oauth2.client.registration.kakao-local.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao-local.client-secret}")
    private String kakaoClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao-local.redirect-uri}")
    private String kakaoLocalRedirectUri;

    private static final String ACCESS_TOKEN_REQUEST_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_REQUEST_URL = "https://kapi.kakao.com/v2/user/me";

    //사용자 토큰 추출
    public String getAccessToken(String authorizationCode, HttpServletRequest request) {
        HttpHeaders headers = createHeaders(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> parameters = createTokenRequestParams(authorizationCode, request);

        return executePostRequest(ACCESS_TOKEN_REQUEST_URL, headers, parameters, "access_token");
    }

    //사용자 정보 요청
    public String getUserInfo(String accessToken) {
        HttpHeaders headers = createHeadersWithAuth(accessToken);

        return executeGetRequest(USER_INFO_REQUEST_URL, headers, "kakao_account", "email");
    }

    private HttpHeaders createHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        return headers;
    }

    private HttpHeaders createHeadersWithAuth(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private MultiValueMap<String, String> createTokenRequestParams(String authorizationCode, HttpServletRequest request) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "authorization_code");
        parameters.add("client_id", kakaoClientId);
        parameters.add("client_secret", kakaoClientSecret);
        parameters.add("redirect_uri", selectRedirectUri(request));
        parameters.add("code", authorizationCode);
        return parameters;
    }

    private String selectRedirectUri(HttpServletRequest request) {
        String originHeader = request.getHeader("Origin");
        return (originHeader != null && originHeader.contains("Loveiw.com"))
                ? "http://localhost:8080/login/oauth2/code/kakao"
                : kakaoLocalRedirectUri;
    }

    private String executePostRequest(String url, HttpHeaders headers, MultiValueMap<String, String> parameters, String key) {
        try {
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
            return extractJsonValue(responseEntity, key, ErrorCode.KAKAO_ACCESS_TOKEN_FAILED, "AccessToken을 얻는 것을 실패했습니다.");
        } catch (Exception e) {
            logger.error("Failed to get access token: {}", e.getMessage());
            throw new UnAuthorizedException("AccessToken을 얻는 것을 실패했습니다.", ErrorCode.KAKAO_ACCESS_TOKEN_FAILED);
        }
    }

    private String executeGetRequest(String url, HttpHeaders headers, String parentKey, String childKey) {
        try {
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return extractJsonValue(responseEntity, parentKey, childKey, ErrorCode.KAKAO_USER_INFO_FAILED, "유저의 정보를 얻지 못했습니다.");
        } catch (Exception e) {
            logger.error("Failed to get user info: {}", e.getMessage());
            throw new UnAuthorizedException("유저의 정보를 얻지 못했습니다.", ErrorCode.KAKAO_USER_INFO_FAILED);
        }
    }

    private String extractJsonValue(ResponseEntity<String> responseEntity, String key, ErrorCode errorCode, String errorMessage) {
        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
            JsonObject jsonObject = JsonParser.parseString(responseEntity.getBody()).getAsJsonObject();
            return jsonObject.get(key).getAsString();
        } else {
            throw new UnAuthorizedException(errorMessage, errorCode);
        }
    }

    private String extractJsonValue(ResponseEntity<String> responseEntity, String parentKey, String childKey, ErrorCode errorCode, String errorMessage) {
        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
            JsonObject jsonObject = JsonParser.parseString(responseEntity.getBody()).getAsJsonObject();
            JsonObject parentObject = jsonObject.getAsJsonObject(parentKey);
            return parentObject.get(childKey).getAsString();
        } else {
            throw new UnAuthorizedException(errorMessage, errorCode);
        }
    }
}
