package solo.project.error;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ErrorCode {
    BAD_REQUEST_EXCEPTION(HttpStatus.BAD_REQUEST, "400", "400 Bad Request"),
    PARAMETER_VALID_EXCEPTION(HttpStatus.BAD_REQUEST, "400", "잘못된 파라미터 값"),
    ACCESS_DENIED_EXCEPTION(HttpStatus.UNAUTHORIZED, "401", "401 UnAuthorized"),
    KAKAO_ACCESS_TOKEN_FAILED(HttpStatus.UNAUTHORIZED, "K4001", "Failed to get access token!"),
    KAKAO_USER_INFO_FAILED(HttpStatus.UNAUTHORIZED, "K4002", "Failed to get user info!"),
    ACCESS_DENIED_BLACKLIST_EXCEPTION(HttpStatus.UNAUTHORIZED, "F4001", "401 BlackList"),
    ACCESS_DENIED_MANAGER_EXCEPTION(HttpStatus.UNAUTHORIZED, "F4002", "401 Not Access Manager"),
    ACCESS_DENIED_USER_EXCEPTION(HttpStatus.UNAUTHORIZED, "F4002", "401 Not Access User"),
    NOT_ALLOW_WRITE_EXCEPTION(HttpStatus.UNAUTHORIZED, "401_NOT_ALLOW", "401 UnAuthorized"),
    FORBIDDEN_EXCEPTION(HttpStatus.FORBIDDEN, "403", "403 Forbidden"),
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "404", "404 Not Found"),
    CONFLICT_EXCEPTION(HttpStatus.CONFLICT, "409", "409 Conflict"),
    INVALID_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "401_Invalid", "Invalid access: token in blacklist"),
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "500", "500 Server Error"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR,"1006","Failed to expired refresh token!"),
    EXPIRED_ACCESS_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR,"1007","Failed to expired access token!"),
    REFRESH_TOKEN_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"2004","Failed to create refresh token!"),
    ACCESS_TOKEN_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"2005","Failed to create access token!");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
