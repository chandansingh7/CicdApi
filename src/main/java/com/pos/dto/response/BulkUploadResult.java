package com.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkUploadResult {
    private int totalRows;
    private int successCount;
    private int failCount;
    private List<RowError> errors;

    @Data
    @Builder
    public static class RowError {
        private int row;      // 1-based Excel row number
        private String field;
        private String message;
    }
}
