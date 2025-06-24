package com.azvtech.file_management.model;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "event-files")
@Schema(description = "Metadata from a stored file")
public class FileMetadata {
    @Id
    private String id;

    @Schema(description = "Original file name", example = "document.pdf")
    private String originalName;

    @Schema(description = "File ID in GridFS", example = "ObjectId('...')")
    private String gridFsId;

    @Schema(description = "MIME file type", example = "application/pdf")
    private String contentType;

    @Schema(description = "Size in bytes", example = "2545")
    private long size;

    @Schema(description = "SHA-256 hash for integrity verification")
    private String checksum;

    private byte[] file;

    private LocalDateTime uploadDate;

    /*
     TODO:  associate with the user
     @ManyToOne
     private User owner;
    */


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getGridFsId() {
        return gridFsId;
    }

    public void setGridFsId(String gridFsId) {
        this.gridFsId = gridFsId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
