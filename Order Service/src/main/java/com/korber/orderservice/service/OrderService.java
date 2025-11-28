package com.korber.orderservice.service;

import com.korber.orderservice.dto.OrderRequest;
import com.korber.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrders();
}
