package com.azvtech.file_management.model;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "event-files")
@Schema(description = "Metadata from a stored file")
public record  FileMetadata (
        @Id
        String id,

        @Schema(description = "Original file name", example = "document.pdf")
        String originalName,

        @Schema(description = "File ID in GridFS", example = "ObjectId('...')")
        String gridFsId,

        @Schema(description = "MIME file type", example = "application/pdf")
        String contentType,

        @Schema(description = "Size in bytes", example = "2545")
        long size,

        @Schema(description = "SHA-256 hash for integrity verification")
        String checksum,

        byte[] file,

        LocalDateTime uploadDate

        /*
         TODO:  associate with the user
         @ManyToOne
         private User owner;
        */
){
    public static final class Builder {
        private String id;
        private String originalName;
        private String gridFsId;
        private String contentType;
        private long size;
        private String checksum;
        private byte[] file;
        private LocalDateTime uploadDate;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder originalName(String originalName) {
            this.originalName = originalName;
            return this;
        }

        public Builder gridFsId(String gridFsId) {
            this.gridFsId = gridFsId;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder file(byte[] file) {
            this.file = file;
            return this;
        }

        public Builder uploadDate(LocalDateTime uploadDate) {
            this.uploadDate = uploadDate;
            return this;
        }

        public FileMetadata build() {
            return new FileMetadata(id, originalName, gridFsId, contentType,
                    size, checksum, file, uploadDate);
        }
    }
}
