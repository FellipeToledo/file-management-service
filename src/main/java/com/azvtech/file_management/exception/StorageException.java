package com.azvtech.file_management.exception;

public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class DuplicateFileException extends StorageException {
        public DuplicateFileException(String filename) {
            super("A file with name '" + filename + "' already exists");
        }
    }

    public static class InvalidFileException extends StorageException {
        public InvalidFileException(String message) {
            super(message);
        }
    }
}
