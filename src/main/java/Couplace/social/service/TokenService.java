package Couplace.social.service;

import Couplace.social.repository.TokenResponse;

public interface TokenService {
    TokenResponse reissueAccessToken(String authorizationHeader);
}
