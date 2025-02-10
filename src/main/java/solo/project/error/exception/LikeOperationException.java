package solo.project.error.exception;

import solo.project.error.ErrorCode;

public class LikeOperationException extends BusinessException {
    public LikeOperationException(String massage, ErrorCode errorCode) {
        super(massage, errorCode);
    }
}
