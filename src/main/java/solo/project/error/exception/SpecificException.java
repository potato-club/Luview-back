package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class SpecificException extends BusinessException{
    public SpecificException(String massage, ErrorCode errorCode) {
        super(massage, errorCode);
    }
}
