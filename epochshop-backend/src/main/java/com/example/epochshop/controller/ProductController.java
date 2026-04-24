package com.example.epochshop.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.epochshop.entity.Product;
import com.example.epochshop.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品查詢與新增 API")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "取得所有商品（支援分頁與關鍵字搜尋）")
    @GetMapping
    public Page<Product> getAllProducts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.searchProducts(keyword, pageable);
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return productService.updateProduct(id, product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateProduct(@PathVariable Long id) {
        productService.reactivateProduct(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Page<Product> getAllProductsForAdmin(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return productService.searchAllProducts(keyword, pageable);
    }
}