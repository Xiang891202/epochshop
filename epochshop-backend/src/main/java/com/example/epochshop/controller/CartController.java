package com.example.epochshop.controller;

import java.util.UUID;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epochshop.dto.CartItemRequest;
import com.example.epochshop.dto.CartResponse;
import com.example.epochshop.service.CartService;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "購物車管理", description = "購物車 CRUD 與冪等 Token API")
public class CartController {
    private final CartService cartService;

    @Operation(summary = "取得購物車冪等 Token")
    @GetMapping("/idempotent-token")
    public ResponseEntity<Map<String, String>> getCartIdempotentToken() {
        String token = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Operation(summary = "查詢購物車內容")
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

     @Operation(summary = "加入商品至購物車（需冪等 Token）")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@RequestBody Map<String, Object> payload) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        Integer quantity = (Integer) payload.get("quantity");
        String idempotentKey = (String) payload.get("idempotentKey");

        if (idempotentKey == null || idempotentKey.isEmpty()) {
            throw new RuntimeException("缺少冪等金鑰");
        }

        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);

        return ResponseEntity.ok(cartService.addItemToCart(request, idempotentKey));
    }

    @Operation(summary = "更新購物車商品數量")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long itemId,
                                                   @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(itemId, quantity));
    }

    @Operation(summary = "刪除購物車品項")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(itemId));
    }
}