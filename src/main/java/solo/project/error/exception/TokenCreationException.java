package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class TokenCreationException extends BusinessException {
    public TokenCreationException(String massage, ErrorCode errorCode) {
        super(massage, errorCode);
    }
}
