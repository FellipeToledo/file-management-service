package com.azvtech.file_management.storage;

import com.azvtech.file_management.exception.StorageException;
import com.azvtech.file_management.exception.StorageFileNotFoundException;
import com.azvtech.file_management.model.FileMetadata;
import com.azvtech.file_management.repository.FileMetadataRepository;
import com.azvtech.file_management.validation.FileValidator;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class StorageServiceImpl implements StorageService {

    private final GridFsService gridFsService;
    private final FileValidator fileValidator;
    private final FileMetadataRepository metadataRepo;

    @Autowired
    public StorageServiceImpl(
            @Value("#{'${storage.allowed-mime-types}'.split(',')}") Set<String> allowedMimeTypes,
            @Value("#{'${storage.allowed-extensions}'.split(',')}") Set<String> allowedExtensions,
            @Value("${storage.max-file-size-mb}") long maxFileSizeMB,
            FileMetadataRepository metadataRepo,
            GridFsService gridFsService) {

        this.fileValidator = new FileValidator(
                allowedMimeTypes,
                allowedExtensions,
                maxFileSizeMB * 1024 * 1024
        );
        this.metadataRepo = metadataRepo;
        this.gridFsService = gridFsService;
    }

    @Override
    public void init() {
    }

    @Override
    public void store(MultipartFile file) {
        fileValidator.validate(file);

        try {
            String gridFsId = gridFsService.storeFile(file);
            saveFileMetadata(file, gridFsId);
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    private void saveFileMetadata(MultipartFile file, String gridFsId) throws IOException {
        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalName(file.getOriginalFilename());
        metadata.setGridFsId(gridFsId);
        metadata.setContentType(file.getContentType());
        metadata.setSize(file.getSize());
        metadata.setChecksum(calculateChecksum(file));
        metadata.setUploadDate(LocalDateTime.now());
        metadataRepo.save(metadata);
    }

    @Override
    public void storeMultiple(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new StorageException("No files sent.");
        }

        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (!file.isEmpty()) {
                    store(file);
                }
            } catch (StorageException e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
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
    public FileMetadata findByOriginalName(String originalName) {
        return metadataRepo.findByOriginalName(originalName);
    }

    @Override
    public List<FileMetadata> findAll() {
        return metadataRepo.findAll();
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
