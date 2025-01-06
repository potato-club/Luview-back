package solo.project.error;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;
import solo.project.error.exception.BadRequestException;
import solo.project.error.exception.DuplicateException;
import solo.project.error.exception.NotFoundException;
import solo.project.error.exception.UnAuthorizedException;


@RequiredArgsConstructor
@RestControllerAdvice
public class ErrorExceptionControllerAdvice {

    private final RestTemplate restTemplate;

    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<ErrorEntity> exceptionHandler(final BadRequestException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorEntity.builder()
                        .errorCode(e.getErrorCode().getCode())
                        .errorMessage(e.getMessage())
                        .build());
    }

    @ExceptionHandler({BindException.class})
    public ResponseEntity<ErrorEntity> exceptionHandler(final BindException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorEntity.builder()
                        .errorCode(ErrorCode.PARAMETER_VALID_EXCEPTION.getCode())
                        .errorMessage(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
                        .build());
    }

    @ExceptionHandler({UnAuthorizedException.class})
    public ResponseEntity<ErrorEntity> exceptionHandler(final UnAuthorizedException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorEntity.builder()
                        .errorCode(e.getErrorCode().getCode())
                        .errorMessage(e.getMessage())
                        .build());
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<ErrorEntity> exceptionHandler(final NotFoundException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorEntity.builder()
                        .errorCode(e.getErrorCode().getCode())
                        .errorMessage(e.getMessage())
                        .build());
    }

    @ExceptionHandler({DuplicateException.class})
    public ResponseEntity<ErrorEntity> exceptionHandler(final DuplicateException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorEntity.builder()
                        .errorCode(e.getErrorCode().getCode())
                        .errorMessage(e.getMessage())
                        .build());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorEntity> exceptionHandler(final MethodArgumentNotValidException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorEntity.builder()
                        .errorCode("400")
                        .errorMessage(e.getAllErrors().get(0).getDefaultMessage())
                        .build());
    }

}

