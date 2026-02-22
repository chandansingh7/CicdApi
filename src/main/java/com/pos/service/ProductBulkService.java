package com.pos.service;

import com.pos.dto.response.BulkUploadResult;
import com.pos.entity.Category;
import com.pos.entity.Inventory;
import com.pos.entity.Product;
import com.pos.repository.CategoryRepository;
import com.pos.repository.InventoryRepository;
import com.pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductBulkService {

    private static final int HEADER_ROW = 0;
    private static final int COL_NAME = 0;
    private static final int COL_SKU = 1;
    private static final int COL_BARCODE = 2;
    private static final int COL_PRICE = 3;
    private static final int COL_CATEGORY = 4;
    private static final int COL_INITIAL_STOCK = 5;
    private static final int COL_LOW_STOCK_THRESHOLD = 6;

    private final ProductRepository   productRepository;
    private final CategoryRepository  categoryRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public BulkUploadResult processUpload(MultipartFile file, String updatedBy) {
        String name = file.getOriginalFilename();
        boolean isCsv = name != null && name.toLowerCase().endsWith(".csv");

        try (InputStream is = file.getInputStream()) {
            return isCsv ? processCsv(is, updatedBy) : processExcel(is, updatedBy);
        } catch (Exception e) {
            log.error("Bulk upload failed", e);
            return BulkUploadResult.builder()
                    .totalRows(0)
                    .successCount(0)
                    .failCount(1)
                    .errors(List.of(BulkUploadResult.RowError.builder()
                            .row(0)
                            .field("file")
                            .message("Failed to read file: " + e.getMessage())
                            .build()))
                    .build();
        }
    }

    private BulkUploadResult processExcel(InputStream is, String updatedBy) throws Exception {
        List<BulkUploadResult.RowError> errors = new ArrayList<>();
        int successCount = 0;
        int updatedCount = 0;
        int totalRows = 0;

        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);
        int lastRow = sheet.getLastRowNum();
        totalRows = Math.max(0, lastRow);
        workbook.close();

        if (lastRow < 1) {
            return BulkUploadResult.builder()
                    .totalRows(0)
                    .successCount(0)
                    .updatedCount(0)
                    .failCount(1)
                    .errors(List.of(BulkUploadResult.RowError.builder()
                            .row(1)
                            .field("file")
                            .message("No data rows. Use row 1 for headers, data from row 2.")
                            .build()))
                    .build();
        }

        for (int r = 1; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            String nameVal = getCellString(row.getCell(COL_NAME));
            if (nameVal == null || nameVal.isBlank()) continue;

            RowResult rowResult = mapRowToProduct(row, r + 1, errors, updatedBy);
            if (rowResult != null) {
                try {
                    if (rowResult.isUpdate() && rowResult.existingInventory() != null) {
                        productRepository.save(rowResult.product());
                        inventoryRepository.save(rowResult.existingInventory());
                        updatedCount++;
                    } else {
                        Product product = productRepository.save(rowResult.product());
                        inventoryRepository.save(Inventory.builder()
                                .product(product)
                                .quantity(rowResult.initialStock())
                                .lowStockThreshold(rowResult.lowStockThreshold())
                                .build());
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("Bulk upload row {} save failed: {}", r + 1, e.getMessage());
                    errors.add(BulkUploadResult.RowError.builder()
                            .row(r + 1)
                            .field("save")
                            .message(e.getMessage())
                            .build());
                }
            }
        }

        return BulkUploadResult.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .updatedCount(updatedCount)
                .failCount(errors.size())
                .errors(errors)
                .build();
    }

    private BulkUploadResult processCsv(InputStream is, String updatedBy) {
        List<BulkUploadResult.RowError> errors = new ArrayList<>();
        int successCount = 0;
        int updatedCount = 0;
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            int rowNum = 0;
            reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;
                String[] cells = parseCsvLine(line);
                if (cells.length <= COL_NAME) continue;
                String nameVal = cellAt(cells, COL_NAME);
                if (nameVal == null || nameVal.isBlank()) continue;

                RowResult rowResult = mapCsvRowToProduct(cells, rowNum + 1, errors, updatedBy);
                if (rowResult != null) {
                    try {
                        if (rowResult.isUpdate() && rowResult.existingInventory() != null) {
                            productRepository.save(rowResult.product());
                            inventoryRepository.save(rowResult.existingInventory());
                            updatedCount++;
                        } else {
                            Product product = productRepository.save(rowResult.product());
                            inventoryRepository.save(Inventory.builder()
                                    .product(product)
                                    .quantity(rowResult.initialStock())
                                    .lowStockThreshold(rowResult.lowStockThreshold())
                                    .build());
                            successCount++;
                        }
                    } catch (Exception e) {
                        log.warn("Bulk upload CSV row {} save failed: {}", rowNum + 1, e.getMessage());
                        errors.add(BulkUploadResult.RowError.builder()
                                .row(rowNum + 1)
                                .field("save")
                                .message(e.getMessage())
                                .build());
                    }
                }
                totalRows = rowNum;
            }
        } catch (Exception e) {
            log.error("CSV parse failed", e);
            errors.add(BulkUploadResult.RowError.builder()
                    .row(0)
                    .field("file")
                    .message("Failed to read CSV: " + e.getMessage())
                    .build());
        }

        return BulkUploadResult.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .updatedCount(updatedCount)
                .failCount(errors.size())
                .errors(errors)
                .build();
    }

    private static String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString().trim().replace("\"\"", "\""));
                cur = new StringBuilder();
            } else if (c == '\n' || c == '\r') {
                break;
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString().trim().replace("\"\"", "\""));
        return out.toArray(new String[0]);
    }

    private static String cellAt(String[] cells, int index) {
        if (index >= cells.length) return null;
        String s = cells[index];
        return s == null || s.isBlank() ? null : s.trim();
    }

    private RowResult mapCsvRowToProduct(String[] cells, int rowNum, List<BulkUploadResult.RowError> errors, String updatedBy) {
        String name = cellAt(cells, COL_NAME);
        if (name == null || name.isBlank()) return null;

        String sku = cellAt(cells, COL_SKU);
        String barcode = cellAt(cells, COL_BARCODE);
        BigDecimal price = parseBigDecimal(cellAt(cells, COL_PRICE));
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            errors.add(BulkUploadResult.RowError.builder().row(rowNum).field("Price").message("Invalid or missing price").build());
            return null;
        }
        Category category = resolveCategory(cellAt(cells, COL_CATEGORY));
        int initialStock = parseInt(cellAt(cells, COL_INITIAL_STOCK), 0);
        int lowStockThreshold = parseInt(cellAt(cells, COL_LOW_STOCK_THRESHOLD), 10);

        if (sku != null && !sku.isBlank()) {
            Optional<Product> existingOpt = productRepository.findBySku(sku.trim());
            if (existingOpt.isPresent()) {
                Product existing = existingOpt.get();
                existing.setName(name.trim());
                existing.setBarcode(barcode != null && !barcode.isBlank() ? barcode.trim() : null);
                existing.setPrice(price);
                existing.setCategory(category);
                existing.setUpdatedBy(updatedBy);
                Optional<Inventory> invOpt = inventoryRepository.findByProductId(existing.getId());
                if (invOpt.isPresent()) {
                    Inventory inv = invOpt.get();
                    inv.setQuantity(initialStock);
                    inv.setLowStockThreshold(lowStockThreshold);
                    inv.setUpdatedBy(updatedBy);
                    return new RowResult(existing, initialStock, lowStockThreshold, true, inv);
                }
            }
        }
        if (barcode != null && !barcode.isBlank() && productRepository.existsByBarcode(barcode)) {
            errors.add(BulkUploadResult.RowError.builder().row(rowNum).field("Barcode").message("Barcode already exists: " + barcode).build());
            return null;
        }

        Product product = Product.builder()
                .name(name.trim())
                .sku(sku != null && !sku.isBlank() ? sku.trim() : null)
                .barcode(barcode != null && !barcode.isBlank() ? barcode.trim() : null)
                .price(price)
                .category(category)
                .active(true)
                .updatedBy(updatedBy)
                .build();
        return new RowResult(product, initialStock, lowStockThreshold);
    }

    private static BigDecimal parseBigDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static int parseInt(String s, int defaultValue) {
        if (s == null || s.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private RowResult mapRowToProduct(Row row, int rowNum, List<BulkUploadResult.RowError> errors, String updatedBy) {
        String name = getCellString(row.getCell(COL_NAME));
        if (name == null || name.isBlank()) return null;

        String sku = getCellString(row.getCell(COL_SKU));
        String barcode = getCellString(row.getCell(COL_BARCODE));
        BigDecimal price = getCellBigDecimal(row.getCell(COL_PRICE));
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            errors.add(BulkUploadResult.RowError.builder().row(rowNum).field("Price").message("Invalid or missing price").build());
            return null;
        }

        Category category = resolveCategory(getCellString(row.getCell(COL_CATEGORY)));
        int initialStock = getCellInt(row.getCell(COL_INITIAL_STOCK), 0);
        int lowStockThreshold = getCellInt(row.getCell(COL_LOW_STOCK_THRESHOLD), 10);

        if (sku != null && !sku.isBlank()) {
            Optional<Product> existingOpt = productRepository.findBySku(sku.trim());
            if (existingOpt.isPresent()) {
                Product existing = existingOpt.get();
                existing.setName(name.trim());
                existing.setBarcode(barcode != null && !barcode.isBlank() ? barcode.trim() : null);
                existing.setPrice(price);
                existing.setCategory(category);
                existing.setUpdatedBy(updatedBy);
                Optional<Inventory> invOpt = inventoryRepository.findByProductId(existing.getId());
                if (invOpt.isPresent()) {
                    Inventory inv = invOpt.get();
                    inv.setQuantity(initialStock);
                    inv.setLowStockThreshold(lowStockThreshold);
                    inv.setUpdatedBy(updatedBy);
                    return new RowResult(existing, initialStock, lowStockThreshold, true, inv);
                }
            }
        }
        if (barcode != null && !barcode.isBlank() && productRepository.existsByBarcode(barcode)) {
            errors.add(BulkUploadResult.RowError.builder().row(rowNum).field("Barcode").message("Barcode already exists: " + barcode).build());
            return null;
        }

        Product product = Product.builder()
                .name(name.trim())
                .sku(sku != null && !sku.isBlank() ? sku.trim() : null)
                .barcode(barcode != null && !barcode.isBlank() ? barcode.trim() : null)
                .price(price)
                .category(category)
                .active(true)
                .updatedBy(updatedBy)
                .build();

        return new RowResult(product, initialStock, lowStockThreshold);
    }

    private Category resolveCategory(String categoryStr) {
        if (categoryStr == null || categoryStr.isBlank()) return null;
        categoryStr = categoryStr.trim();
        Optional<Category> byName = categoryRepository.findByName(categoryStr);
        if (byName.isPresent()) return byName.get();
        try {
            Long id = Long.parseLong(categoryStr);
            return categoryRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    private static BigDecimal getCellBigDecimal(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue();
                if (s == null || s.isBlank()) return null;
                return new BigDecimal(s.trim());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static int getCellInt(Cell cell, int defaultValue) {
        if (cell == null) return defaultValue;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            }
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue();
                if (s == null || s.isBlank()) return defaultValue;
                return Integer.parseInt(s.trim());
            }
        } catch (Exception e) {
            return defaultValue;
        }
        return defaultValue;
    }

    public byte[] generateExcelTemplate() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Products");
            Row header = sheet.createRow(0);
            String[] headers = { "Name", "SKU", "Barcode", "Price", "Category", "Initial Stock", "Low Stock Threshold" };
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("Example Product");
            exampleRow.createCell(1).setCellValue("SKU-001");
            exampleRow.createCell(2).setCellValue("1234567890123");
            exampleRow.createCell(3).setCellValue(9.99);
            exampleRow.createCell(4).setCellValue("Electronics");
            exampleRow.createCell(5).setCellValue(100);
            exampleRow.createCell(6).setCellValue(10);
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate template", e);
        }
    }

    public byte[] generateCsvTemplate() {
        String header = "Name,SKU,Barcode,Price,Category,Initial Stock,Low Stock Threshold";
        String example = "Example Product,SKU-001,1234567890123,9.99,Electronics,100,10";
        return (header + "\n" + example).getBytes(StandardCharsets.UTF_8);
    }

    /** Returns which of the given SKUs already exist (non-null, non-blank only). */
    public List<String> findExistingSkus(List<String> skus) {
        if (skus == null || skus.isEmpty()) return List.of();
        List<String> toCheck = skus.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (toCheck.isEmpty()) return List.of();
        return productRepository.findSkusBySkuIn(toCheck);
    }

    private record RowResult(Product product, int initialStock, int lowStockThreshold, boolean isUpdate, Inventory existingInventory) {
        RowResult(Product product, int initialStock, int lowStockThreshold) {
            this(product, initialStock, lowStockThreshold, false, null);
        }
    }
}
