package com.korber.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for inventory update responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateResponse {
    private boolean success;
    private String message;
    private Long productId;
    private Integer updatedQuantity;
}
