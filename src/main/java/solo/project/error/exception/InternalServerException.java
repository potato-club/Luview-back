package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class InternalServerException extends BusinessException {

    public InternalServerException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
