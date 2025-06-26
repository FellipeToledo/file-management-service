package com.azvtech.file_management.storage;

import com.azvtech.file_management.model.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public sealed interface StorageService permits StorageServiceImpl{

    default void init() {
    }

    void store(MultipartFile file);

    void storeMultiple(MultipartFile[] files);

    Resource loadAsResource(String filename);

    FileMetadata findByOriginalName(String originalName);

    List<FileMetadata> findAll();

    void delete(String originalName);
}
