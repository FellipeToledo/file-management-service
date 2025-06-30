package com.azvtech.file_management.validation;
import com.azvtech.file_management.exception.StorageException;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public final class FileValidator {

    private final Set<String> allowedMimeTypes;
    private final Set<String> allowedExtensions;
    private final long maxFileSize;

    public FileValidator(Set<String> allowedMimeTypes, Set<String> allowedExtensions, long maxFileSize) {
        this.allowedMimeTypes = Set.copyOf(allowedMimeTypes);
        this.allowedExtensions = Set.copyOf(allowedExtensions);
        this.maxFileSize = maxFileSize;
    }

    public void validate(MultipartFile file) {
        if (file == null) {
            throw new StorageException.InvalidFileException("File cannot be null");
        }

        validateEmptyFile(file);
        validateFileSize(file);
        validateContentType(file);
        validateFileExtension(file);
        validateMimeTypeConsistency(file);
    }

    private void validateEmptyFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException.InvalidFileException("Empty file not allowed");
        }
    }


    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new StorageException(
                    "File size exceeds %dMB limit".formatted(maxFileSize / (1024 * 1024)));
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType)) {
            throw new StorageException("MIME type '%s' is not supported".formatted(contentType));
        }
    }

    private void validateFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf(".") == -1) {
            throw new StorageException("Invalid file name");
        }

        String fileExtension = originalFilename.substring(
                originalFilename.lastIndexOf(".") + 1).toLowerCase();

        if (!allowedExtensions.contains(fileExtension)) {
            throw new StorageException("Extension '.%s' is not allowed".formatted(fileExtension));
        }
    }

    private void validateMimeTypeConsistency(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null) {
            throw new StorageException("Invalid file name");
        }

        String fileExtension = originalFilename.substring(
                originalFilename.lastIndexOf(".") + 1).toLowerCase();

        if (!isMimeTypeMatchesExtension(contentType, fileExtension)) {
            throw new StorageException("File extension does not match the declared MIME type");
        }
    }

    private boolean isMimeTypeMatchesExtension(String mimeType, String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf".equals(mimeType);
            case "jpg", "jpeg" -> "image/jpeg".equals(mimeType);
            case "png" -> "image/png".equals(mimeType);
            case "gif" -> "image/gif".equals(mimeType);
            case "txt" -> "text/plain".equals(mimeType);
            default -> true;
        };
    }
}
