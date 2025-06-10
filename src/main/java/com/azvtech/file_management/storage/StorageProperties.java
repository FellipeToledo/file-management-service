package com.azvtech.file_management.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties("storage")
public class StorageProperties {

    private String location = "upload-dir";
    private List<String> allowedMimeTypes;
    private List<String> allowedExtensions;

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

   public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }
    public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
}
