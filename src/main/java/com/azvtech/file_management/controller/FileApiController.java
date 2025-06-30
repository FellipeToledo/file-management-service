package com.azvtech.file_management.controller;

import com.azvtech.file_management.dto.response.ErrorResponseDto;
import com.azvtech.file_management.model.FileMetadata;
import com.azvtech.file_management.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Validated
@RestController
@RequestMapping("/api/v1/file")
@Tag(name = "File API", description = "File management operations")
@RequiredArgsConstructor
public class FileApiController {

    private final StorageService storageService;

    @GetMapping("/{originalName:.+}")
    @Operation(
            summary = "File download/viewing",
            description = "Download the file or display it in the browser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "File found"),
                    @ApiResponse(responseCode = "404", description = "File not found",
                            content = @Content(schema = @Schema(hidden = true)))
            },
            parameters = {
                    @Parameter(name = "filename", description = "Name of the stored file"),
                    @Parameter(name = "view", description = "If ‘true’, try displaying it in the browser")
            })
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String originalName,
            @RequestParam(required = false) @Parameter(description = "Display in browser if true") Boolean view
            ) throws IOException {

        FileMetadata metadata = storageService.findByOriginalName(originalName);
        Resource file = storageService.loadAsResource(metadata.gridFsId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        Boolean.TRUE.equals(view) && isViewable(metadata.contentType()) ?
                                "inline" : "attachment; filename=\"" + metadata.originalName() + "\"")
                .contentType(MediaType.parseMediaType(metadata.contentType()))
                .body(file);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload single file",
            description = "Upload a file to the server",
            responses = {
                    @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid file",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "File with this name already exists",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            })
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") @Parameter(description = "File to upload") MultipartFile file) {
        storageService.store(file);
        return ResponseEntity.ok(Map.of(
                "message", "Upload successful: " + file.getOriginalFilename(),
                "filename", Objects.requireNonNull(file.getOriginalFilename())
        ));
    }


    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload multiple files",
            description = "Upload multiple files to the server",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Files uploaded successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid files")
            })
    public ResponseEntity<Map<String, String>> uploadMultipleFiles(
            @RequestParam("files") @Parameter(description = "Files to upload") List<MultipartFile> files) {

        storageService.storeMultiple(files);
        return ResponseEntity.ok(Map.of(
                "message", files.size() + " files uploaded successfully",
                "count", String.valueOf(files.size())
        ));
    }

    @GetMapping
    @Operation(
            summary = "List all files",
            description = "Get metadata for all stored files",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of file metadata")
            })
    public ResponseEntity<List<FileMetadata>> listAllFiles() {
        return ResponseEntity.ok(storageService.loadAllMetadata());
    }

    @DeleteMapping("/{originalName:.+}")
    @Operation(
            summary = "Delete file",
            description = "Delete a file from the server",
            responses = {
                    @ApiResponse(responseCode = "200", description = "File deleted"),
                    @ApiResponse(responseCode = "404", description = "File not found")
            })
    public ResponseEntity<Void> deleteFile(
            @PathVariable @Parameter(description = "Name of file to delete") String originalName) {

        storageService.delete(originalName);
        return ResponseEntity.ok().build();
    }

    private boolean isViewable(String contentType) {
        return contentType != null && (
                contentType.startsWith("image/") ||
                        contentType.equals("application/pdf") ||
                        contentType.startsWith("text/")
        );
    }
}
