package edu.usip.document.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    String storeFile(MultipartFile file, String sourceId) throws IOException;

    Resource getFile(String storagePath) throws IOException;

    void deleteFile(String storagePath) throws IOException;
}