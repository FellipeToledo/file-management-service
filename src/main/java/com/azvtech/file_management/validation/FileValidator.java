package com.azvtech.file_management.validation;


import com.azvtech.file_management.exception.StorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public class FileValidator {

    private final Set<String> allowedMimeTypes;
    private final Set<String> allowedExtensions;
    private final long maxFileSize;

    public FileValidator(Set<String> allowedMimeTypes,
                         Set<String> allowedExtensions,
                         long maxFileSize) {
        this.allowedMimeTypes = Set.copyOf(allowedMimeTypes);
        this.allowedExtensions = Set.copyOf(allowedExtensions);
        this.maxFileSize = maxFileSize;
    }

    public void validate(MultipartFile file) {
        if (file == null) {
            throw new StorageException("Arquivo não pode ser nulo");
        }

        validateEmptyFile(file);
        validateFileSize(file);
        validateContentType(file);
        validateFileExtension(file);
        validateMimeTypeConsistency(file);
    }

    private void validateEmptyFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Arquivo vazio não permitido");
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new StorageException(
                    String.format("Tamanho do arquivo excede o limite de %dMB",
                            maxFileSize / (1024 * 1024)));
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType)) {
            throw new StorageException(
                    String.format("Tipo MIME '%s' não é suportado", contentType));
        }
    }

    private void validateFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf(".") == -1) {
            throw new StorageException("Nome de arquivo inválido");
        }

        String fileExtension = originalFilename.substring(
                originalFilename.lastIndexOf(".") + 1).toLowerCase();

        if (!allowedExtensions.contains(fileExtension)) {
            throw new StorageException(
                    String.format("Extensão '.%s' não é permitida", fileExtension));
        }
    }

    private void validateMimeTypeConsistency(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String fileExtension = originalFilename.substring(
                originalFilename.lastIndexOf(".") + 1).toLowerCase();

        if (!isMimeTypeMatchesExtension(contentType, fileExtension)) {
            throw new StorageException(
                    "Extensão do arquivo não corresponde ao tipo MIME declarado");
        }
    }

    private boolean isMimeTypeMatchesExtension(String mimeType, String extension) {
        return switch (extension) {
            case "pdf" -> mimeType.equals("application/pdf");
            case "jpg", "jpeg" -> mimeType.equals("image/jpeg");
            case "png" -> mimeType.equals("image/png");
            case "gif" -> mimeType.equals("image/gif");
            case "txt" -> mimeType.equals("text/plain");
            default -> true;
        };
    }
}
