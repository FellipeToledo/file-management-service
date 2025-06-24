package com.azvtech.file_management.controller;

import com.azvtech.file_management.exception.StorageException;
import com.azvtech.file_management.model.FileMetadata;
import com.azvtech.file_management.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/file")
@Tag(name = "File API", description = "File management operations")
public class FileApiController {

    private final StorageService storageService;

    @Autowired
    public FileApiController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/download/{originalName:.+}")
    @Operation(
            summary = "File download/viewing",
            description = "Download the file or display it in the browser",
            parameters = {
                    @Parameter(name = "filename", description = "Name of the stored file"),
                    @Parameter(name = "view", description = "If ‘true’, try displaying it in the browser")
            })
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String originalName,
            @RequestParam(required = false) String view) throws IOException {

        FileMetadata metadata = storageService.findByOriginalName(originalName);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }

        Resource file = storageService.loadAsResource(metadata.getGridFsId());

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "true".equals(view) && isViewable(metadata.getContentType()) ?
                                "inline" : "attachment")
                .header("filename", metadata.getOriginalName())
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .body(file);
    }



    @PostMapping("/upload")
    @Operation(
            summary = "Single file upload",
            description = "Sends a file to the server",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful upload"),
                    @ApiResponse(responseCode = "400", description = "Upload error")
            })
    public ResponseEntity<Map<String, String>> handleFileUpload(
            @RequestParam("file") MultipartFile file) {

        try {
            storageService.store(file);
            return ResponseEntity.ok(Map.of(
                    "message", "Upload successful: " + file.getOriginalFilename()
            ));
        } catch (StorageException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Upload multiple files",
            description = "Send multiple files to the server",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful upload"),
                    @ApiResponse(responseCode = "400", description = "Upload error")
            })
    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, String>> handleMultipleFileUpload(
            @RequestParam("files") MultipartFile[] files) {

        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "No files selected"
                ));
            }

            storageService.storeMultiple(files);
            return ResponseEntity.ok(Map.of(
                    "message", files.length + " files were sent successfully"
            ));
        } catch (StorageException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    private boolean isViewable(String contentType) {
        return contentType != null && (
                contentType.startsWith("image/") ||
                        contentType.equals("application/pdf") ||
                        contentType.startsWith("text/")
        );
    }
}
