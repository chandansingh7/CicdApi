package com.pos.controller;

import com.pos.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Serves locally stored product images (dev fallback only).
 * In production, images are served directly from Azure Blob Storage CDN.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService imageStorageService;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> serve(@PathVariable String filename) {
        try {
            byte[] data = imageStorageService.loadLocal(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, imageStorageService.contentType(filename))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .body(data);
        } catch (IOException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
