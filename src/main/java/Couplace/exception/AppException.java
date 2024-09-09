package Couplace.exception;

public class AppException extends RuntimeException {
    private final ErrorCodes errorCode;

    public AppException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }
}
