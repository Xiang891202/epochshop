package com.example.epochshop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epochshop.entity.Product;
import com.example.epochshop.entity.User;
import com.example.epochshop.repository.ProductRepository;
import com.example.epochshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ✅ 获取当前用户（从 SecurityContext）
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAllActive(pageable);
        }
        return productRepository.searchByName(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> searchAllProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.searchByName(keyword, pageable);
    }

    @Transactional
    public Product createProduct(Product product) {
        product.setSeller(getCurrentUser());
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setImageUrl(product.getImageUrl());
        return productRepository.save(existing);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public void reactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(true);
        productRepository.save(product);
    }
}