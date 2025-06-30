package com.azvtech.file_management.storage;

import com.azvtech.file_management.config.StorageProperties;
import com.azvtech.file_management.exception.StorageException;
import com.azvtech.file_management.exception.StorageFileNotFoundException;
import com.azvtech.file_management.model.FileMetadata;
import com.azvtech.file_management.repository.FileMetadataRepository;
import com.azvtech.file_management.validation.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public final class StorageServiceImpl implements StorageService {

    private final boolean allowDuplicateFiles;
    private final GridFsService gridFsService;
    private final FileValidator fileValidator;
    private final FileMetadataRepository metadataRepo;

    public StorageServiceImpl(
            StorageProperties storageProperties,
            FileMetadataRepository metadataRepo,
            GridFsService gridFsService) {
        this.allowDuplicateFiles = storageProperties.allowDuplicateFiles();
        this.fileValidator = new FileValidator(
                storageProperties.allowedMimeTypes(),
                storageProperties.allowedExtensions(),
                storageProperties.maxFileSizeMb() * 1024 * 1024
        );
        this.metadataRepo = metadataRepo;
        this.gridFsService = gridFsService;
        log.info("StorageService initialized with max file size: {}MB", storageProperties.maxFileSizeMb());
    }

    @Override
    public void store(MultipartFile file) {
        if (!allowDuplicateFiles && existsByOriginalName(file.getOriginalFilename())) {
            throw new StorageException.DuplicateFileException(file.getOriginalFilename());
        }

        fileValidator.validate(file); // Já lança InvalidFileException se houver erro

        try {
            String gridFsId = gridFsService.storeFile(file);
            saveFileMetadata(file, gridFsId);
            log.info("File stored successfully: {}", file.getOriginalFilename());
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }


    @Override
    public void storeMultiple(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new StorageException("No files sent.");
        }

        List<String> errors = new ArrayList<>();

        for (var file : files) {
            try {
                if (!file.isEmpty()) {
                    store(file);
                }
            } catch (StorageException e) {
                errors.add("%s: %s".formatted(file.getOriginalFilename(), e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            throw new StorageException("Upload errors: " + String.join("; ", errors));
        }
    }

    @Override
    public Resource loadAsResource(String gridFsId) {
        try {
            InputStream inputStream = gridFsService.getFileStream(gridFsId);
            return new InputStreamResource(inputStream);
        } catch (IOException e) {
            throw new StorageFileNotFoundException("Could not read file: " + gridFsId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadata findByOriginalName(String originalName) {
        return metadataRepo.findByOriginalName(originalName)
                .orElseThrow(() -> new StorageFileNotFoundException("File not found: " + originalName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileMetadata> loadAllMetadata() {
        return metadataRepo.findAll();
    }

    @Override
    public void delete(String originalName) {
        FileMetadata metadata = metadataRepo.findByOriginalName(originalName)
                .orElseThrow(() -> new StorageFileNotFoundException("File not found: " + originalName));

        gridFsService.deleteFile(metadata.gridFsId());
        metadataRepo.delete(metadata);
        log.info("File deleted successfully: {}", originalName);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByOriginalName(String originalName) {
        return metadataRepo.findByOriginalName(originalName).isPresent();
    }

    private void saveFileMetadata(MultipartFile file, String gridFsId) throws IOException {
        var metadata = FileMetadata.builder()
                .originalName(file.getOriginalFilename())
                .gridFsId(gridFsId)
                .contentType(file.getContentType())
                .size(file.getSize())
                .checksum(calculateChecksum(file))
                .uploadDate(LocalDateTime.now())
                .build();

        metadataRepo.save(metadata);
    }

    private String calculateChecksum(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(IOUtils.toByteArray(is));
            return Hex.encodeHexString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new StorageException("Failed to calculate checksum", e);
        }
    }
}
