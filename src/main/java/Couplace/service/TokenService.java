package Couplace.service;

import Couplace.repository.TokenResponse;

public interface TokenService {
    TokenResponse reissueAccessToken(String authorizationHeader);
}