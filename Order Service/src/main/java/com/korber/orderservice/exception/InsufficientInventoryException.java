package com.korber.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientInventoryException extends RuntimeException {
    
    public InsufficientInventoryException(String message) {
        super(message);
    }
    
    public InsufficientInventoryException(Long productId, Integer requested, Integer available) {
        super(String.format("Insufficient inventory for product ID %d. Requested: %d, Available: %d", 
                productId, requested, available));
    }
}
