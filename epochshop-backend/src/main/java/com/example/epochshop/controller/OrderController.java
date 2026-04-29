package com.example.epochshop.controller;

import com.example.epochshop.dto.OrderResponse;
import com.example.epochshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "訂單管理", description = "結帳、查詢訂單與模擬付款 API")
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "取得訂單冪等 Token")
    @GetMapping("/idempotent-token")
    public ResponseEntity<Map<String, String>> getIdempotentToken() {
        String token = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Operation(summary = "結帳建立訂單（需冪等 Token）")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody Map<String, String> payload) {
        String idempotentKey = payload.get("idempotentKey");
        if (idempotentKey == null || idempotentKey.isEmpty()) {
            throw new RuntimeException("缺少冪等金鑰");
        }
        return ResponseEntity.ok(orderService.createOrder(idempotentKey));
    }

    @Operation(summary = "查詢目前使用者所有訂單")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders() {
        return ResponseEntity.ok(orderService.getUserOrders());
    }

    @Operation(summary = "模擬付款，更新訂單狀態為已付款")
    @PutMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.payOrder(orderId));
    }
}