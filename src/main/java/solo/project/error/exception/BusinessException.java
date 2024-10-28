package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;

    public BusinessException(String massage, ErrorCode errorCode) {
        super(massage);
        this.errorCode = errorCode;
    }
}
