package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class NotFoundException extends BusinessException{
    public NotFoundException(String massage, ErrorCode errorCode) {
        super(massage, errorCode);
    }
}
