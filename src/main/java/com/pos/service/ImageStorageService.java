package com.pos.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.PublicAccessType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores product images either in Azure Blob Storage (production)
 * or on the local filesystem (dev fallback when AZURE_STORAGE_CONNECTION_STRING is not set).
 *
 * Storage layout (Azure):
 *   Container : pos-product-images  (public read access)
 *   Blob name : products/{productId}/{uuid}.{ext}
 *   Public URL: https://{account}.blob.core.windows.net/pos-product-images/products/{id}/{uuid}.{ext}
 *
 * Storage layout (local fallback):
 *   Directory : {tmpDir}/pos-images/
 *   Served at : GET /api/images/{filename}
 */
@Slf4j
@Service
public class ImageStorageService {

    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    @Value("${azure.storage.container:pos-product-images}")
    private String containerName;

    @Value("${image.local.base-url:http://localhost:8080/api/images}")
    private String localBaseUrl;

    private Path localStorageDir;
    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        if (isAzureConfigured()) {
            try {
                BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();
                containerClient = serviceClient.getBlobContainerClient(containerName);
                if (!containerClient.exists()) {
                    containerClient.createWithResponse(null, PublicAccessType.BLOB, null, null);
                    log.info("Created Azure Blob container: {}", containerName);
                }
                log.info("Azure Blob Storage configured — container: {}", containerName);
            } catch (Exception ex) {
                log.warn("Azure Blob Storage init failed ({}), falling back to local storage", ex.getMessage());
                containerClient = null;
                initLocalStorage();
            }
        } else {
            log.info("AZURE_STORAGE_CONNECTION_STRING not set — using local image storage");
            initLocalStorage();
        }
    }

    private void initLocalStorage() {
        localStorageDir = Paths.get(System.getProperty("java.io.tmpdir"), "pos-images");
        try {
            Files.createDirectories(localStorageDir);
            log.info("Local image storage directory: {}", localStorageDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create local image storage directory", ex);
        }
    }

    /**
     * Stores a company asset (logo or favicon) and returns its public URL.
     *
     * @param type "logo" or "favicon"
     * @param file the uploaded multipart file
     * @return public URL to access the stored image
     */
    public String storeCompanyFile(String type, MultipartFile file) throws IOException {
        String ext = getExtension(file.getOriginalFilename());
        String filename = type + ext;

        if (isAzureConfigured() && containerClient != null) {
            return storeCompanyInAzure(filename, file);
        }
        return storeLocally(filename, file);
    }

    private String storeCompanyInAzure(String filename, MultipartFile file) throws IOException {
        String blobName = "company/" + filename;
        try (InputStream in = file.getInputStream()) {
            containerClient.getBlobClient(blobName)
                    .upload(in, file.getSize(), true);
        }
        String url = containerClient.getBlobContainerUrl() + "/" + blobName;
        log.debug("Stored company file in Azure Blob: {}", url);
        return url;
    }

    /**
     * Stores the given file and returns its public URL.
     *
     * @param productId the product the image belongs to
     * @param file      the uploaded multipart file
     * @return public URL to access the stored image
     */
    public String store(Long productId, MultipartFile file) throws IOException {
        String ext      = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;

        if (isAzureConfigured() && containerClient != null) {
            return storeInAzure(productId, filename, file);
        }
        return storeLocally(filename, file);
    }

    private String storeInAzure(Long productId, String filename, MultipartFile file) throws IOException {
        String blobName = "products/" + productId + "/" + filename;
        try (InputStream in = file.getInputStream()) {
            containerClient.getBlobClient(blobName)
                    .upload(in, file.getSize(), true);
        }
        String url = containerClient.getBlobContainerUrl() + "/" + blobName;
        log.debug("Stored image in Azure Blob: {}", url);
        return url;
    }

    private String storeLocally(String filename, MultipartFile file) throws IOException {
        Path dest = localStorageDir.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        String url = localBaseUrl + "/" + filename;
        log.debug("Stored image locally: {}", url);
        return url;
    }

    /**
     * Serves a locally stored image as a byte array.
     * Only used in dev fallback mode.
     */
    public byte[] loadLocal(String filename) throws IOException {
        Path file = localStorageDir.resolve(filename).normalize();
        if (!file.startsWith(localStorageDir)) {
            throw new SecurityException("Path traversal attempt blocked");
        }
        return Files.readAllBytes(file);
    }

    /**
     * Returns the content-type for a stored filename.
     */
    public String contentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private boolean isAzureConfigured() {
        return connectionString != null && !connectionString.isBlank();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
