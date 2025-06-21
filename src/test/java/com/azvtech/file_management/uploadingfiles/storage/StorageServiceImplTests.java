package com.azvtech.file_management.uploadingfiles.storage;

import com.azvtech.file_management.storage.StorageServiceImpl;
import com.azvtech.file_management.exception.StorageException;
import com.azvtech.file_management.storage.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageServiceImplTests {

    private final StorageProperties properties = new StorageProperties();
    private final List<String> allowedMimeTypes = Arrays.asList("image/jpeg", "application/pdf");
    private final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "pdf");
    private StorageServiceImpl service;


    @BeforeEach
    public void init() {
        properties.setLocation("target/files/" + Math.random());
        service = new StorageServiceImpl(properties, allowedMimeTypes, allowedExtensions);
        service.init();
    }

    @Test
    public void emptyUploadLocation() {
        service = null;
        properties.setLocation("");
        assertThrows(StorageException.class, () -> {
            service = new StorageServiceImpl(properties, allowedMimeTypes, allowedExtensions);
        });
    }

    @Test
    public void loadNonExistent() {
        assertThat(service.load("foo.txt")).doesNotExist();
    }

    @Test
    public void saveAndLoad() {
        service.store(new MockMultipartFile("foo", "foo.txt", MediaType.TEXT_PLAIN_VALUE,
                "Hello, World".getBytes()));
        assertThat(service.load("foo.txt")).exists();
    }

    @Test
    public void saveRelativePathNotPermitted() {
        assertThrows(StorageException.class, () -> {
            service.store(new MockMultipartFile("foo", "../foo.txt",
                    MediaType.TEXT_PLAIN_VALUE, "Hello, World".getBytes()));
        });
    }

    @Test
    public void saveAbsolutePathNotPermitted() {
        assertThrows(StorageException.class, () -> {
            service.store(new MockMultipartFile("foo", "/etc/passwd",
                    MediaType.TEXT_PLAIN_VALUE, "Hello, World".getBytes()));
        });
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    public void saveAbsolutePathInFilenamePermitted() {
        //Unix file systems (e.g. ext4) allows backslash '\' in file names.
        String fileName="\\etc\\passwd";
        service.store(new MockMultipartFile(fileName, fileName,
                MediaType.TEXT_PLAIN_VALUE, "Hello, World".getBytes()));
        assertTrue(Files.exists(
                Paths.get(properties.getLocation()).resolve(Paths.get(fileName))));
    }

    @Test
    public void savePermitted() {
        service.store(new MockMultipartFile("foo", "bar/../foo.txt",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World".getBytes()));
    }

}
