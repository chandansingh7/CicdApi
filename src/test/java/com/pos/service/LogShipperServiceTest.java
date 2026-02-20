package com.pos.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogShipperService")
class LogShipperServiceTest {

    @Mock BlobContainerClient containerClient;
    @Mock BlobClient          blobClient;

    @TempDir Path tempDir;

    private LogShipperService service;

    @BeforeEach
    void setUp() {
        service = new LogShipperService();
        ReflectionTestUtils.setField(service, "connectionString", "DefaultEndpointsProtocol=https;AccountName=test;AccountKey=dGVzdA==;EndpointSuffix=core.windows.net");
        ReflectionTestUtils.setField(service, "containerName",    "pos-logs");
        ReflectionTestUtils.setField(service, "logFileDir",       tempDir.toString());
        ReflectionTestUtils.setField(service, "appName",          "cicdpos");
        ReflectionTestUtils.setField(service, "containerClient",  containerClient);
    }

    @Test
    @DisplayName("shipLogForDate uploads the log file when it exists")
    void shipLogForDate_uploadsWhenFileExists() throws IOException {
        LocalDate date    = LocalDate.now().minusDays(1);
        String    dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Path archiveDir = tempDir.resolve("archive");
        Files.createDirectories(archiveDir);
        Files.writeString(archiveDir.resolve("cicdpos." + dateStr + ".log"), "test log content");

        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);

        service.shipLogForDate(date);

        verify(containerClient).getBlobClient("logs/cicdpos." + dateStr + ".log");
        verify(blobClient).uploadFromFile(anyString(), eq(true));
    }

    @Test
    @DisplayName("shipLogForDate skips upload when log file does not exist")
    void shipLogForDate_skipsWhenFileMissing() {
        service.shipLogForDate(LocalDate.now().minusDays(5));

        verify(containerClient, never()).getBlobClient(any());
        verify(blobClient,      never()).uploadFromFile(any(), anyBoolean());
    }

    @Test
    @DisplayName("shipYesterdaysLog skips when Azure is not configured")
    void shipYesterdaysLog_skipsWhenNotConfigured() {
        ReflectionTestUtils.setField(service, "connectionString", "");
        ReflectionTestUtils.setField(service, "containerClient",  null);

        service.shipYesterdaysLog();

        verifyNoInteractions(containerClient);
    }
}
