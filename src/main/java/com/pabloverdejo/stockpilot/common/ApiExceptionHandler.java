package com.pabloverdejo.stockpilot.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return ApiProblems.create(
                HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(), "not-found", request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail handleBusinessRule(BusinessRuleException exception, HttpServletRequest request) {
        return ApiProblems.create(
                HttpStatus.CONFLICT, "Business rule violation", exception.getMessage(), "business-rule", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var detail = ApiProblems.create(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more fields contain invalid values.", "validation", request);
        var errors = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail handleMethodValidation(HandlerMethodValidationException exception, HttpServletRequest request) {
        var detail = ApiProblems.create(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more request parameters contain invalid values.", "validation", request);
        var errors = new LinkedHashMap<String, String>();
        exception.getParameterValidationResults().forEach(result -> {
            var parameterName = result.getMethodParameter().getParameterName();
            var message = result.getResolvableErrors().stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .filter(value -> value != null && !value.isBlank())
                    .findFirst()
                    .orElse("has an invalid value");
            errors.putIfAbsent(parameterName == null ? "parameter" : parameterName, message);
        });
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return ApiProblems.create(HttpStatus.BAD_REQUEST, "Invalid request body",
                "The request body is malformed or contains an unsupported value.", "invalid-body", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        var detail = ApiProblems.create(HttpStatus.BAD_REQUEST, "Invalid parameter",
                "Parameter '" + exception.getName() + "' contains an unsupported value.",
                "invalid-parameter", request);
        detail.setProperty("parameter", exception.getName());
        return detail;
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
        return ApiProblems.create(HttpStatus.UNAUTHORIZED, "Authentication failed",
                "The supplied credentials are invalid.", "authentication", request);
    }
}
