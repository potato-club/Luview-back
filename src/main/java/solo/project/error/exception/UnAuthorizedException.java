package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class UnAuthorizedException extends BusinessException{

    public UnAuthorizedException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
