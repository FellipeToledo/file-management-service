package com.azvtech.file_management.exception.handler;

import com.azvtech.file_management.dto.response.ErrorResponseDto;
import com.azvtech.file_management.exception.StorageException;
import com.azvtech.file_management.exception.StorageFileNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleStorageFileNotFound(
            StorageFileNotFoundException ex, WebRequest request) {

        return buildErrorResponse(
                ex,
                HttpStatus.NOT_FOUND,
                "File not found",
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponseDto> handleStorageException(
            StorageException ex, WebRequest request) {

        HttpStatus status = ex instanceof StorageException.DuplicateFileException
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        return buildErrorResponse(
                ex,
                status,
                "Storage error",
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<ErrorResponseDto.SubError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponseDto.SubError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponseDto response = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation error")
                .message("Invalid request content")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        List<ErrorResponseDto.SubError> errors = ex.getConstraintViolations().stream()
                .map(violation -> ErrorResponseDto.SubError.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponseDto response = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation error")
                .message("Invalid request parameters")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(
            Exception ex, WebRequest request) {

        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponseDto> buildErrorResponse(
            Exception ex,
            HttpStatus status,
            String error,
            String path) {

        ErrorResponseDto response = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(ex.getMessage())
                .path(path)
                .build();

        return new ResponseEntity<>(response, status);
    }
}
