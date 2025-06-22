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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path rootLocation;
    private final FileValidator fileValidator;
    private final FileMetadataRepository metadataRepo;


    @Autowired
    public StorageServiceImpl(
            StorageProperties properties,
            @Value("${storage.allowed-mime-types}.split(',')}")
            Set<String> allowedMimeTypes,
            @Value("#{'${storage.allowed-extensions}'.split(',')}")
            Set<String> allowedExtensions,
            @Value("${storage.max-file-size-mb}")
            long maxFileSizeMB,
            FileMetadataRepository metadataRepo) {

        this.rootLocation = Paths.get(properties.getLocation());
        this.fileValidator = new FileValidator(
                allowedMimeTypes,
                allowedExtensions,
                maxFileSizeMB * 1024 * 1024
        );
        this.metadataRepo = metadataRepo;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }



    @Override
    public void store(MultipartFile file) {
        fileValidator.validate(file);

        try {
            String storedName = generateStoredFilename(file);
            Path destinationFile = resolveSafePath(storedName);
            saveFileContent(file, destinationFile);
            saveFileMetadata(file, storedName);
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    private String generateStoredFilename(MultipartFile file) {
        return UUID.randomUUID() + "_" + file.getOriginalFilename();
    }

    private Path resolveSafePath(String filename) {
        Path destinationFile = rootLocation.resolve(filename).normalize().toAbsolutePath();
        if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
            throw new StorageException("Cannot store file outside current directory");
        }
        return destinationFile;
    }

    private void saveFileContent(MultipartFile file, Path destinationFile) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void saveFileMetadata(MultipartFile file, String storedName) throws IOException {
        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalName(file.getOriginalFilename());
        metadata.setStoredName(storedName);
        metadata.setContentType(file.getContentType());
        metadata.setSize(file.getSize());
        metadata.setChecksum(calculateChecksum(file));
        metadata.setUploadDate(LocalDateTime.now());
        metadataRepo.save(metadata);
    }


    @Override
    public void storeMultiple(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new StorageException("Nenhum arquivo enviado.");
        }

        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            System.out.println(file.getContentType());
            System.out.println(file.getName());
            System.out.println(file.getSize());
            System.out.println(file.getOriginalFilename());

            try {
                if (!file.isEmpty()) {
                    store(file);
                }
            } catch (StorageException e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new StorageException("Erros no upload: " + String.join("; ", errors));
        }
    }

    @Override
    public boolean existsById(Long id) {
        return metadataRepo.existsById(id);
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

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public List<FileMetadata> findAll() {
        return metadataRepo.findAll();
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public FileMetadata findByStoredName(String storedName) {
        return metadataRepo.findByStoredName(storedName);
    }
}
