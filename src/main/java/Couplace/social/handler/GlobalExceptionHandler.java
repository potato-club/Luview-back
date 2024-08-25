package Couplace.social.handler;

import Couplace.social.dto.UserErrorResult;
import Couplace.social.dto.UserException;
import Couplace.social.security.BaseErrorCode;
import Couplace.social.api.ApiResponse;
import Couplace.social.token.TokenErrorResult;
import Couplace.social.token.TokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ApiResponse<BaseErrorCode>> handleTokenException(TokenException e) {
        TokenErrorResult errorResult = e.getTokenErrorResult();
        return ApiResponse.onFailure(errorResult);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<BaseErrorCode>> handleUserException(UserException e) {
        UserErrorResult errorResult = e.getUserErrorResult();
        return ApiResponse.onFailure(errorResult);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/500";  // custom error page
    }
}