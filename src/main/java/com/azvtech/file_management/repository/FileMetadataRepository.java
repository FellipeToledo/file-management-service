package com.azvtech.file_management.repository;

import com.azvtech.file_management.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    FileMetadata findByStoredName(String storedName);
}
