package com.korber.inventoryservice.service;

import com.korber.inventoryservice.dto.InventoryBatchDTO;
import com.korber.inventoryservice.dto.InventoryUpdateRequest;
import com.korber.inventoryservice.dto.InventoryUpdateResponse;
import com.korber.inventoryservice.factory.InventoryHandler;
import com.korber.inventoryservice.factory.InventoryHandlerFactory;
import com.korber.inventoryservice.model.InventoryBatch;
import com.korber.inventoryservice.model.Product;
import com.korber.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the InventoryService interface
 */
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    
    private final ProductRepository productRepository;
    private final InventoryHandlerFactory inventoryHandlerFactory;
    
    @Override
    public List<InventoryBatchDTO> getInventoryBatchesByProductId(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        Product product = productOpt.get();
        InventoryHandler handler = inventoryHandlerFactory.getDefaultHandler();
        
        List<InventoryBatch> batches = handler.getInventoryBatchesByExpiryDate(product);
        
        // Convert to DTOs
        return batches.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {
        String handlerType = request.getHandlerType();
        InventoryHandler handler = (handlerType != null && !handlerType.isEmpty()) 
                ? inventoryHandlerFactory.getHandler(handlerType)
                : inventoryHandlerFactory.getDefaultHandler();
        
        boolean success = handler.updateInventory(request.getProductId(), request.getQuantity());
        
        InventoryUpdateResponse response = new InventoryUpdateResponse();
        response.setSuccess(success);
        response.setProductId(request.getProductId());
        response.setUpdatedQuantity(request.getQuantity());
        
        if (success) {
            response.setMessage("Inventory updated successfully");
        } else {
            response.setMessage("Failed to update inventory. Insufficient stock or product not found.");
        }
        
        return response;
    }
    
    /**
     * Convert InventoryBatch entity to DTO
     * @param batch The inventory batch entity
     * @return The inventory batch DTO
     */
    private InventoryBatchDTO convertToDTO(InventoryBatch batch) {
        InventoryBatchDTO dto = new InventoryBatchDTO();
        dto.setId(batch.getId());
        dto.setBatchNumber(batch.getBatchNumber());
        dto.setQuantity(batch.getQuantity());
        dto.setExpiryDate(batch.getExpiryDate());
        dto.setProductId(batch.getProduct().getId());
        return dto;
    }
}
