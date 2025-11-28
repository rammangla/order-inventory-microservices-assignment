package com.korber.inventoryservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is insufficient inventory to fulfill a request
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientInventoryException extends RuntimeException {
    
    public InsufficientInventoryException(String message) {
        super(message);
    }
    
    public InsufficientInventoryException(Long productId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient inventory for product ID %d. Requested: %d, Available: %d", 
                productId, requestedQuantity, availableQuantity));
    }
}
