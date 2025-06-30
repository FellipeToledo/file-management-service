package com.azvtech.file_management.storage;

import com.azvtech.file_management.model.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public sealed interface StorageService permits StorageServiceImpl{

    default void init() {
    }

    void store(MultipartFile file);

    void storeMultiple(List<MultipartFile> files);

    Resource loadAsResource(String filename);

    FileMetadata findByOriginalName(String originalName);

    List<FileMetadata> loadAllMetadata();

    void delete(String originalName);

    @Transactional(readOnly = true)
    boolean existsByOriginalName(String originalName);
}
