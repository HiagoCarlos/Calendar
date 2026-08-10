package br.com.calendar.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @Override
    protected ResponseEntity<@NonNull Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
                                                                           HttpStatusCode status, WebRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid fields");
        problem.setProperty("fields", fields);
        return handleExceptionInternal(e, problem, headers, status, request);
    }

    // Anything ResponseEntityExceptionHandler already knows how to handle
    // (ResponseStatusException and friends, used throughout the services)
    // is resolved by its own, more specific handlers before this one is
    // considered. This only catches genuinely unexpected exceptions, so they
    // don't leak internals (stack traces, messages) to the client.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception e, WebRequest request) {
        log.error("Unexpected error", e);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        return handleExceptionInternal(e, problem, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
