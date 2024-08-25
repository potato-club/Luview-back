package Couplace.social.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class TokenResponse {
    @JsonProperty("access_token")
    private String access_token;
}
