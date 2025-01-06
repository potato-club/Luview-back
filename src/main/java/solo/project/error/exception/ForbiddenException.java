package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}