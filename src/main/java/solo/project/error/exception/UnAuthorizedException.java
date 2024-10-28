package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class UnAuthorizedException extends BusinessException{

    public UnAuthorizedException(String massage, ErrorCode errorCode) {
        super(massage, errorCode);
    }
}
