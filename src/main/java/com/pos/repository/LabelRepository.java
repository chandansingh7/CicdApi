package com.pos.repository;

import com.pos.entity.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    @Query("SELECT l FROM Label l WHERE l.product IS NULL AND " +
           "(LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.barcode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(l.sku, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Label> searchUnlinked(@Param("query") String query, Pageable pageable);

    @Query("SELECT l FROM Label l WHERE l.product IS NULL")
    Page<Label> findAllUnlinked(Pageable pageable);

    @Query("SELECT l FROM Label l WHERE l.product IS NULL AND l.category.id = :categoryId")
    Page<Label> findByCategoryIdAndUnlinked(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT l FROM Label l WHERE l.product IS NULL AND l.category.id = :categoryId AND " +
           "(LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.barcode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(l.sku, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Label> searchUnlinkedByCategory(@Param("query") String query, @Param("categoryId") Long categoryId, Pageable pageable);
}
