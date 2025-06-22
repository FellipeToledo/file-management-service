package com.azvtech.file_management.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Schema(description = "Metadados de um arquivo armazenado")
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Nome original do arquivo", example = "documento.pdf")
    @Column(nullable = false)
    private String originalName;

    @Schema(description = "Nome físico no sistema", example = "UUID + extensão")
    @Column(nullable = false)
    private String storedName;

    @Schema(description = "Tipo MIME do arquivo", example = "application/pdf")
    @Column(nullable = false)
    private String contentType;

    @Schema(description = "Tamanho em bytes", example = "2545")
    @Column(nullable = false)
    private long size;

    @Schema(description = "Hash SHA-256 para verificação de integridade")
    @Column(nullable = false)
    private String checksum;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    // TODO: associar ao usuário
    // @ManyToOne
    // private User owner;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
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
