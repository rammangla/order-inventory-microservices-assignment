package com.korber.inventoryservice.factory;

import com.korber.inventoryservice.model.InventoryBatch;
import com.korber.inventoryservice.model.Product;
import com.korber.inventoryservice.repository.InventoryBatchRepository;
import com.korber.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Standard implementation of the InventoryHandler interface
 * Uses FIFO (First In First Out) strategy based on expiry date
 */
@Component
@RequiredArgsConstructor
public class StandardInventoryHandler implements InventoryHandler {
    
    private final ProductRepository productRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    
    @Override
    public List<InventoryBatch> getInventoryBatchesByExpiryDate(Product product) {
        List<InventoryBatch> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(product.getId());
        return batches;
    }
    
    @Override
    @Transactional
    public boolean updateInventory(Long productId, int quantityToReduce) {
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            return false;
        }
        
        Product product = productOpt.get();
        List<InventoryBatch> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);
        
        int remainingQuantity = quantityToReduce;
        
        for (InventoryBatch batch : batches) {
            if (remainingQuantity <= 0) {
                break;
            }
            
            int currentBatchQuantity = batch.getQuantity();
            
            if (currentBatchQuantity <= remainingQuantity) {
                // Use up entire batch
                remainingQuantity -= currentBatchQuantity;
                batch.setQuantity(0);
            } else {
                // Use part of the batch
                batch.setQuantity(currentBatchQuantity - remainingQuantity);
                remainingQuantity = 0;
            }
            
            inventoryBatchRepository.save(batch);
        }
        
        // If we couldn't fulfill the entire quantity, return false
        return remainingQuantity == 0;
    }
    
    @Override
    public String getType() {
        return "STANDARD";
    }
}
