package Couplace.social.constant;

public enum Provider {
    KAKAO_PROVIDER("kakao"),
    GOOGLE_PROVIDER("google"),
    NAVER_PROVIDER("naver");

    private final String provider;

    Provider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
