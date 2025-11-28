package com.korber.inventoryservice.service;

import com.korber.inventoryservice.dto.InventoryBatchDTO;
import com.korber.inventoryservice.dto.InventoryUpdateRequest;
import com.korber.inventoryservice.dto.InventoryUpdateResponse;

import java.util.List;

/**
 * Service interface for inventory operations
 */
public interface InventoryService {
    
    /**
     * Get inventory batches for a product sorted by expiry date
     * @param productId The product ID
     * @return List of inventory batches sorted by expiry date
     */
    List<InventoryBatchDTO> getInventoryBatchesByProductId(Long productId);
    
    /**
     * Update inventory after an order is placed
     * @param request The inventory update request
     * @return Response indicating success or failure
     */
    InventoryUpdateResponse updateInventory(InventoryUpdateRequest request);
}
