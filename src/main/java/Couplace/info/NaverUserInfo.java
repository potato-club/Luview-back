package Couplace.info;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class NaverUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response != null && response.containsKey("id")) {
            return (String) response.get("id");
        }
        throw new IllegalArgumentException("Missing 'id' in response attributes");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getName() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response != null && response.containsKey("name")) {
            return (String) response.get("name");
        }
        throw new IllegalArgumentException("Missing 'name' in response attributes");
    }
}
