package com.azvtech.file_management.repository;

import com.azvtech.file_management.model.FileMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    FileMetadata findByOriginalName(String originalName);

}
