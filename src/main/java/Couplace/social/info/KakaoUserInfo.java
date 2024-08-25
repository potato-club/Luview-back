package Couplace.social.info;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        if (id instanceof Long) {
            return String.valueOf(id); // Long 타입을 String으로 변환
        } else if (id instanceof String) {
            return (String) id;
        } else {
            throw new IllegalArgumentException("Invalid ID type: " + id.getClass().getName());
        }
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties != null) {
            Object nickname = properties.get("nickname");
            if (nickname instanceof String) {
                return (String) nickname;
            } else {
                System.out.println("Nickname is not a String, actual type: " + (nickname != null ? nickname.getClass().getName() : "null"));
            }
        }
        return "Main"; // nickname을 찾지 못한 경우 기본값 반환
    }

}
