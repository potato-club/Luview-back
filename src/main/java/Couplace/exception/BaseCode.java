package Couplace.exception;

import Couplace.dto.ReasonDto;

public interface BaseCode {
    public ReasonDto getReason();

    public ReasonDto getReasonHttpStatus();
}