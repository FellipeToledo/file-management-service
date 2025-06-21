package com.azvtech.file_management.storage;

import com.azvtech.file_management.exception.StorageException;
import com.azvtech.file_management.exception.StorageFileNotFoundException;
import com.azvtech.file_management.model.FileMetadata;
import com.azvtech.file_management.repository.FileMetadataRepository;
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
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path rootLocation;

    @Value("${storage.allowed-mime-types}")
    private final List<String> allowedMimeTypes;

    @Value("${storage.allowed-extensions}")
    private final List<String> allowedExtensions;

    private final FileMetadataRepository metadataRepo;


    @Autowired
    public StorageServiceImpl(StorageProperties properties, List<String> allowedMimeTypes, List<String> allowedExtensions, FileMetadataRepository metadataRepo) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.allowedMimeTypes = allowedMimeTypes;
        this.allowedExtensions = allowedExtensions;
        this.metadataRepo = metadataRepo;
    }

    @Override
    public void store(MultipartFile file) {
        try {
            // Validação 1: Arquivo vazio
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            // Validação 2: Tipo MIME permitido
            String fileContentType = file.getContentType();
            if (!allowedMimeTypes.contains(fileContentType)) {
                throw new StorageException("Tipo de arquivo não suportado: " + fileContentType);
            }
            // Validação 3: Extensão permitida (backup para MIME inválido)
            String originalFilename = file.getOriginalFilename();
            assert originalFilename != null;
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            // Gera um nome único para o arquivo
            String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            if (!allowedExtensions.contains(fileExtension)) {
                throw new StorageException("Extensão de arquivo não permitida: " + fileExtension);
            }
            // Salva o arquivo no sistema
            Path destinationFile = this.rootLocation.resolve(storedName).normalize().toAbsolutePath();
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }

            // Calcula o checksum (SHA-256)
            String checksum = calculateChecksum(file);

            // Salva metadados no banco
            FileMetadata metadata = new FileMetadata();
            metadata.setOriginalName(file.getOriginalFilename());
            metadata.setStoredName(storedName);
            metadata.setContentType(file.getContentType());
            metadata.setSize(file.getSize());
            metadata.setChecksum(checksum);
            metadata.setUploadDate(LocalDateTime.now());

            metadataRepo.save(metadata);

        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
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
            throw new StorageException("Falha ao calcular checksum.", e);
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

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }


}
