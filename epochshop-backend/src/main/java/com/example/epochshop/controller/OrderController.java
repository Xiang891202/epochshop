package com.example.epochshop.controller;

import com.example.epochshop.dto.OrderResponse;
import com.example.epochshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder() {
        return ResponseEntity.ok(orderService.createOrder());
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders() {
        return ResponseEntity.ok(orderService.getUserOrders());
    }

    @PutMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.payOrder(orderId));
    }
}