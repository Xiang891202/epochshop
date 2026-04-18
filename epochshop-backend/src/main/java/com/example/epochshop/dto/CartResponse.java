package com.example.epochshop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponse {
    private Long cartId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;

    @Data
    public static class CartItemResponse {
        private Long itemId;
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}