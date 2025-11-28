package com.korber.inventoryservice.factory;

import com.korber.inventoryservice.model.InventoryBatch;
import com.korber.inventoryservice.model.Product;

import java.util.List;

/**
 * Interface defining the contract for inventory handling strategies
 */
public interface InventoryHandler {
    
    /**
     * Get inventory batches for a product sorted by expiry date
     * @param product The product to get batches for
     * @return List of inventory batches sorted by expiry date
     */
    List<InventoryBatch> getInventoryBatchesByExpiryDate(Product product);
    
    /**
     * Update inventory after an order is placed
     * @param productId The product ID
     * @param quantity The quantity to reduce from inventory
     * @return true if inventory was updated successfully, false otherwise
     */
    boolean updateInventory(Long productId, int quantity);
    
    /**
     * Get the type of inventory handler
     * @return The inventory handler type
     */
    String getType();
}
