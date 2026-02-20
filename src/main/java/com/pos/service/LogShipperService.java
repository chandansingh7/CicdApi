package com.pos.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.PublicAccessType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Uploads rolled daily log files to Azure Blob Storage.
 *
 * Logback rolls the active log at midnight:
 *   logs/archive/cicdpos.yyyy-MM-dd.log
 *
 * This service runs every day at 00:05 (5 minutes after roll) and uploads
 * yesterday's log file to the configured blob container:
 *   Blob path: logs/cicdpos.yyyy-MM-dd.log
 *
 * Requires AZURE_STORAGE_CONNECTION_STRING to be set; skipped in dev otherwise.
 */
@Slf4j
@Service
public class LogShipperService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    @Value("${log.blob.container:pos-logs}")
    private String containerName;

    @Value("${log.file.dir:./logs}")
    private String logFileDir;

    @Value("${spring.application.name:cicdpos}")
    private String appName;

    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        if (!isAzureConfigured()) {
            log.info("Log shipper: Azure not configured — log shipping to Blob Storage is disabled");
            return;
        }
        try {
            BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            containerClient = serviceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                // Private container — logs are sensitive
                containerClient.create();
                log.info("Log shipper: created Blob container '{}'", containerName);
            }
            log.info("Log shipper: Azure Blob Storage ready — container '{}'", containerName);
        } catch (Exception ex) {
            log.warn("Log shipper: failed to initialise Azure Blob client — {}", ex.getMessage());
            containerClient = null;
        }
    }

    /**
     * Runs at 00:05 every day.
     * Uploads the previous day's archived log file to Azure Blob Storage.
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void shipYesterdaysLog() {
        if (!isAzureConfigured() || containerClient == null) return;

        String dateStr  = LocalDate.now().minusDays(1).format(DATE_FMT);
        Path   logFile  = Paths.get(logFileDir, "archive", appName + "." + dateStr + ".log");

        if (!Files.exists(logFile)) {
            log.warn("Log shipper: log file not found for {}: {}", dateStr, logFile);
            return;
        }

        String blobName = "logs/" + appName + "." + dateStr + ".log";
        try {
            log.info("Log shipper: uploading {} → blob:{}", logFile.getFileName(), blobName);
            containerClient.getBlobClient(blobName)
                    .uploadFromFile(logFile.toAbsolutePath().toString(), true);
            log.info("Log shipper: upload complete — {}", blobName);
        } catch (Exception ex) {
            log.error("Log shipper: upload failed for {} — {}", blobName, ex.getMessage(), ex);
        }
    }

    /**
     * Allows manual trigger of log shipping for a specific date,
     * useful for backfilling missed uploads (call via Actuator or test).
     */
    public void shipLogForDate(LocalDate date) {
        if (!isAzureConfigured() || containerClient == null) {
            log.warn("Log shipper: Azure not configured, cannot ship log for {}", date);
            return;
        }

        String dateStr  = date.format(DATE_FMT);
        Path   logFile  = Paths.get(logFileDir, "archive", appName + "." + dateStr + ".log");

        if (!Files.exists(logFile)) {
            log.warn("Log shipper: log file not found for date {}: {}", dateStr, logFile);
            return;
        }

        String blobName = "logs/" + appName + "." + dateStr + ".log";
        try {
            containerClient.getBlobClient(blobName)
                    .uploadFromFile(logFile.toAbsolutePath().toString(), true);
            log.info("Log shipper: manually shipped log for {} → blob:{}", dateStr, blobName);
        } catch (Exception ex) {
            log.error("Log shipper: manual upload failed for {} — {}", blobName, ex.getMessage(), ex);
        }
    }

    private boolean isAzureConfigured() {
        return connectionString != null && !connectionString.isBlank();
    }
}
