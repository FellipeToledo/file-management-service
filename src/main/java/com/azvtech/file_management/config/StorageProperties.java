package com.azvtech.file_management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Set;

@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        @DefaultValue({"image/jpeg", "image/png", "application/pdf"})
        Set<String> allowedMimeTypes,

        @DefaultValue({"jpg", "jpeg", "png", "pdf"})
        Set<String> allowedExtensions,

        @DefaultValue("50")
        long maxFileSizeMb,

        @DefaultValue("false")
        boolean allowDuplicateFiles

) {}