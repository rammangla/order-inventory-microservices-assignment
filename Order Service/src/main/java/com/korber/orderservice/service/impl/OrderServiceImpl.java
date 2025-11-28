package com.korber.orderservice.service.impl;

import com.korber.orderservice.dto.*;
import com.korber.orderservice.exception.InsufficientInventoryException;
import com.korber.orderservice.exception.ResourceNotFoundException;
import com.korber.orderservice.model.Order;
import com.korber.orderservice.model.OrderItem;
import com.korber.orderservice.repository.OrderRepository;
import com.korber.orderservice.service.InventoryService;
import com.korber.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // Check inventory for all products
        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            List<InventoryResponse> inventoryItems = inventoryService.checkInventory(itemRequest.getProductId());
            
            // Calculate total available quantity
            int availableQuantity = inventoryItems.stream()
                    .mapToInt(InventoryResponse::getQuantity)
                    .sum();
            
            if (availableQuantity < itemRequest.getQuantity()) {
                throw new InsufficientInventoryException(
                        itemRequest.getProductId(), 
                        itemRequest.getQuantity(), 
                        availableQuantity
                );
            }
        }
        
        // Create order entity
        Order order = new Order();
        order.setCustomerName(orderRequest.getCustomerName());
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        
        // Calculate total amount and add order items
        double totalAmount = 0.0;
        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(itemRequest.getPrice());
            orderItem.setHandlerType(itemRequest.getHandlerType());
            
            order.addOrderItem(orderItem);
            totalAmount += itemRequest.getPrice() * itemRequest.getQuantity();
            
            // Update inventory
            InventoryUpdateRequest updateRequest = InventoryUpdateRequest.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .handlerType(itemRequest.getHandlerType())
                    .build();
            
            inventoryService.updateInventory(updateRequest);
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(item -> {
                    OrderItemResponse response = new OrderItemResponse();
                    response.setId(item.getId());
                    response.setProductId(item.getProductId());
                    response.setQuantity(item.getQuantity());
                    response.setPrice(item.getPrice());
                    response.setHandlerType(item.getHandlerType());
                    return response;
                })
                .collect(Collectors.toList());
        
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setOrderItems(orderItemResponses);
        response.setTotalAmount(order.getTotalAmount());
        return response;
    }
}
