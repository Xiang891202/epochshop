package com.example.epochshop.entity;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Version
    @Schema(description = "樂觀鎖版本號", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer version;

    // Getter / Setter 保持不變（此處省略，請保留原有的即可）
}