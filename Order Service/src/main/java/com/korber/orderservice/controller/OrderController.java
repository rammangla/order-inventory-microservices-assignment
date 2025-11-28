package com.korber.orderservice.controller;

import com.korber.orderservice.dto.OrderRequest;
import com.korber.orderservice.dto.OrderResponse;
import com.korber.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "API for order management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(
        summary = "Place a new order",
        description = "Places a new order and updates inventory accordingly",
        responses = {
            @ApiResponse(
                responseCode = "201", 
                description = "Order created successfully",
                content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient inventory"),
            @ApiResponse(responseCode = "503", description = "Inventory service unavailable")
        }
    )
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = orderService.createOrder(orderRequest);
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves an order by its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Order found",
                content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Order not found")
        }
    )
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse orderResponse = orderService.getOrderById(id);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    @Operation(
        summary = "Get all orders",
        description = "Retrieves all orders in the system",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Orders retrieved successfully"
            )
        }
    )
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}
