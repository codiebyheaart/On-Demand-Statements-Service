package com.ibm.cmod.ondemand.service;

import com.ibm.cmod.ondemand.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for managing file system storage
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.storage.location:./storage/afp-files}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(storageLocation);
        try {
            Files.createDirectories(rootLocation);
            logger.info("Initialized file storage at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to initialize file storage", e);
            throw new FileStorageException("Failed to initialize file storage", e);
        }
    }

    /**
     * Store file in file system
     */
    public String storeFile(String filename, byte[] fileData) {
        try {
            if (filename.contains("..")) {
                throw new FileStorageException("Invalid filename: " + filename);
            }

            Path destinationFile = rootLocation.resolve(filename).normalize().toAbsolutePath();

            // Create parent directories if they don't exist
            Files.createDirectories(destinationFile.getParent());

            // Write file
            Files.write(destinationFile, fileData);

            logger.info("Stored file: {} ({} bytes)", filename, fileData.length);
            return destinationFile.toString();

        } catch (IOException e) {
            logger.error("Failed to store file: {}", filename, e);
            throw new FileStorageException("Failed to store file: " + filename, e);
        }
    }

    /**
     * Load file from file system
     */
    public Resource loadFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                logger.debug("Loaded file: {}", filename);
                return resource;
            } else {
                logger.error("File not found or not readable: {}", filename);
                throw new FileStorageException("File not found or not readable: " + filename);
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed URL for file: {}", filename, e);
            throw new FileStorageException("Malformed URL for file: " + filename, e);
        }
    }

    /**
     * Delete file from file system
     */
    public void deleteFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
            logger.info("Deleted file: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filename, e);
            throw new FileStorageException("Failed to delete file: " + filename, e);
        }
    }

    /**
     * Get file size
     */
    public long getFileSize(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            return Files.size(file);
        } catch (IOException e) {
            logger.error("Failed to get file size: {}", filename, e);
            return 0;
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String filename) {
        Path file = rootLocation.resolve(filename).normalize();
        return Files.exists(file);
    }
}
