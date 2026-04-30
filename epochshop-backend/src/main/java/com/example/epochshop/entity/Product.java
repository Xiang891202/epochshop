package com.example.epochshop.entity;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "商品ID", example = "1")
    private Long id;

    @Schema(description = "商品名稱", example = "iPhone 15 示範機")
    private String name;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品價格", example = "29900.00")
    private BigDecimal price;

    @Schema(description = "庫存數量", example = "10")
    private Integer stock;

    @Column(length = 500)
    @Schema(description = "商品圖片網址")
    private String imageUrl;

    @Version
    @Schema(description = "樂觀鎖版本號", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer version;

    // ✅ 新增：商品狀態（true=上架, false=下架）
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;
}