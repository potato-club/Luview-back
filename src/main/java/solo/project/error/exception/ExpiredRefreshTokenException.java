package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class ExpiredRefreshTokenException extends BusinessException{
    public ExpiredRefreshTokenException(String massage, ErrorCode errorCode) {
        super(massage, errorCode);
    }
}
