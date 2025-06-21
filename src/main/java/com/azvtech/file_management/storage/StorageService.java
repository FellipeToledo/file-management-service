package com.azvtech.file_management.storage;

import com.azvtech.file_management.model.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    void storeMultiple(MultipartFile[] files);

    boolean existsById(Long id);

    Stream<Path> loadAll();

    List<FileMetadata> findAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

    FileMetadata findByStoredName(String storedName);

}
