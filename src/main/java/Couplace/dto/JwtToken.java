package Couplace.dto;

import lombok.Getter;

@Getter
public class JwtToken {
    private String asseceToken;
    private String refreshToken;

    public JwtToken(String asseceToken, String refreshToken) {
        this.asseceToken = asseceToken;
        this.refreshToken = refreshToken;
    }
}
