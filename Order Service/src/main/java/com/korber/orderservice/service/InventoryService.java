package com.korber.orderservice.service;

import com.korber.orderservice.dto.InventoryResponse;
import com.korber.orderservice.dto.InventoryUpdateRequest;

import java.util.List;

public interface InventoryService {
    List<InventoryResponse> checkInventory(Long productId);
    void updateInventory(InventoryUpdateRequest request);
}
