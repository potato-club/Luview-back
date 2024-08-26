package Couplace.exception;

import Couplace.dto.ErrorReasonDto;

public interface BaseErrorCode {
    public ErrorReasonDto getReason();

    public ErrorReasonDto getReasonHttpStatus();
}