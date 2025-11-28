package com.korber.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for InventoryBatch entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatchDTO {
    private Long id;
    private String batchNumber;
    private Integer quantity;
    private LocalDate expiryDate;
    private Long productId;
}
