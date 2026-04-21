package com.example.epochshop.controller;

import com.example.epochshop.dto.OrderResponse;
import com.example.epochshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/idempotent-token")
    public ResponseEntity<Map<String, String>> getIdempotentToken() {
        String token = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody Map<String, String> payload) {
        String idempotentKey = payload.get("idempotentKey");
        if (idempotentKey == null || idempotentKey.isEmpty()) {
            throw new RuntimeException("缺少冪等金鑰");
        }
        return ResponseEntity.ok(orderService.createOrder(idempotentKey));
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