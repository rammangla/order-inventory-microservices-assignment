package com.korber.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for inventory update requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateRequest {
    private Long productId;
    private Integer quantity;
    private String handlerType;
}
