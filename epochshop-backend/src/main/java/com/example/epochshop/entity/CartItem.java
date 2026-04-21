package com.example.epochshop.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cart_id", "product_id"}),
    @UniqueConstraint(columnNames = {"idempotent_key"})  // ✅ 新增冪等鍵唯一約束
})
@Data
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantity;

    // 儲存加入時的單價，避免後續商品變價影響歷史資料
    private BigDecimal unitPrice;

    @Column(name = "idempotent_key", unique = true, length = 36)  // ✅ 新增欄位
    private String idempotentKey;
}