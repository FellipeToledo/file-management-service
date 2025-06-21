package com.azvtech.file_management.controller;

import com.azvtech.file_management.exception.StorageException;
import com.azvtech.file_management.model.FileMetadata;
import com.azvtech.file_management.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/file")
public class FileApiController {

    private final StorageService storageService;

    @Autowired
    public FileApiController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/list")
    public List<FileMetadata> listFiles() {
        return storageService.findAll();
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String filename,
            @RequestParam(required = false) String view) throws IOException {

        Resource file = storageService.loadAsResource(filename);
        FileMetadata metadata = storageService.findByStoredName(filename);

        if (file == null || metadata == null) {
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
    public ResponseEntity<Map<String, String>> handleFileUpload(
            @RequestParam("file") MultipartFile file) {

        try {
            storageService.store(file);
            return ResponseEntity.ok(Map.of(
                    "message", "Upload realizado com sucesso: " + file.getOriginalFilename()
            ));
        } catch (StorageException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, String>> handleMultipleFileUpload(
            @RequestParam("files") MultipartFile[] files) {

        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Nenhum arquivo selecionado"
                ));
            }

            storageService.storeMultiple(files);
            return ResponseEntity.ok(Map.of(
                    "message", files.length + " arquivos foram enviados com sucesso"
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
