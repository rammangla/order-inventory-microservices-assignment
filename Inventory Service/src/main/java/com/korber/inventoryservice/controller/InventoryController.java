package com.korber.inventoryservice.controller;

import com.korber.inventoryservice.dto.InventoryBatchDTO;
import com.korber.inventoryservice.dto.InventoryUpdateRequest;
import com.korber.inventoryservice.dto.InventoryUpdateResponse;
import com.korber.inventoryservice.exception.ResourceNotFoundException;
import com.korber.inventoryservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for inventory operations
 */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory Management API")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    /**
     * Get inventory batches for a product sorted by expiry date
     * @param productId The product ID
     * @return List of inventory batches sorted by expiry date
     */
    @GetMapping("/{productId}")
    @Operation(
        summary = "Get inventory batches by product ID",
        description = "Returns a list of inventory batches for the specified product, sorted by expiry date"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory batches", 
                    content = @Content(schema = @Schema(implementation = InventoryBatchDTO.class))),
        @ApiResponse(responseCode = "404", description = "Product not found", 
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class)))
    })
    public ResponseEntity<List<InventoryBatchDTO>> getInventoryBatchesByProductId(
            @Parameter(description = "ID of the product to retrieve inventory for", required = true) 
            @PathVariable Long productId) {
        List<InventoryBatchDTO> batches = inventoryService.getInventoryBatchesByProductId(productId);
        
        if (batches.isEmpty()) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        
        return ResponseEntity.ok(batches);
    }
    
    /**
     * Update inventory after an order is placed
     * @param request The inventory update request
     * @return Response indicating success or failure
     */
    @PostMapping("/update")
    @Operation(
        summary = "Update inventory",
        description = "Updates inventory after an order is placed"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventory updated successfully", 
                    content = @Content(schema = @Schema(implementation = InventoryUpdateResponse.class))),
        @ApiResponse(responseCode = "400", description = "Failed to update inventory", 
                    content = @Content(schema = @Schema(implementation = InventoryUpdateResponse.class)))
    })
    public ResponseEntity<InventoryUpdateResponse> updateInventory(
            @Parameter(description = "Inventory update request details", required = true) 
            @RequestBody InventoryUpdateRequest request) {
        InventoryUpdateResponse response = inventoryService.updateInventory(request);
        
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }
}
