package com.korber.orderservice.service.impl;

import com.korber.orderservice.dto.InventoryResponse;
import com.korber.orderservice.dto.InventoryUpdateRequest;
import com.korber.orderservice.exception.InventoryServiceException;
import com.korber.orderservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Override
    public List<InventoryResponse> checkInventory(Long productId) {
        try {
            String url = inventoryServiceUrl + "/inventory/" + productId;
            ResponseEntity<List<InventoryResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<InventoryResponse>>() {}
            );
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error checking inventory for product ID {}: {}", productId, e.getMessage());
            throw new InventoryServiceException("Failed to check inventory for product ID " + productId, e);
        }
    }

    @Override
    public void updateInventory(InventoryUpdateRequest request) {
        try {
            String url = inventoryServiceUrl + "/inventory/update";
            restTemplate.postForEntity(url, request, Void.class);
        } catch (RestClientException e) {
            log.error("Error updating inventory for product ID {}: {}", request.getProductId(), e.getMessage());
            throw new InventoryServiceException("Failed to update inventory for product ID " + request.getProductId(), e);
        }
    }
}
