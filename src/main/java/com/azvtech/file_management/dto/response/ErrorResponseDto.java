package com.azvtech.file_management.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "Error response structure")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        @Schema(description = "Timestamp of the error", example = "2025-06-26T18:30:45.12345")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Error type", example = "Bad Request")
        String error,

        @Schema(description = "Detailed error message", example = "Invalid file format")
        String message,

        @Schema(description = "API path where the error occurred", example = "/api/v1/files/upload")
        String path,

        @Schema(description = "List of sub-errors (optional)", nullable = true)
        List<SubError> errors
) {
    @Builder
    public record SubError(
            @Schema(description = "Error field", example = "file")
            String field,

            @Schema(description = "Error message", example = "File size exceeds limit")
            String message,

            @Schema(description = "Rejected value", example = "52428800")
            Object rejectedValue
    ) {}
}