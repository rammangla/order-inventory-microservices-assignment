package com.korber.inventoryservice.repository;

import com.korber.inventoryservice.model.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for InventoryBatch entity
 */
@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    
    /**
     * Find all inventory batches for a product sorted by expiry date in ascending order
     * @param productId The product ID
     * @return List of inventory batches sorted by expiry date
     */
    List<InventoryBatch> findByProductIdOrderByExpiryDateAsc(Long productId);
}
