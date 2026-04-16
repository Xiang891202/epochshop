package com.example.epochshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epochshop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
