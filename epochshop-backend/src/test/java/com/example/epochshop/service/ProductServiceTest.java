package com.example.epochshop.service;

import com.example.epochshop.entity.Product;
import com.example.epochshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("29900"));
        product.setStock(10);
    }

    @Test
    void searchProducts_WithKeyword_ShouldCallSearchByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(product));
        when(productRepository.searchByName(anyString(), any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> result = productService.searchProducts("iPhone", pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(productRepository, times(1)).searchByName("iPhone", pageable);
        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void searchProducts_WithBlankKeyword_ShouldCallFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> result = productService.searchProducts("   ", pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(productRepository, times(1)).findAll(pageable);
        verify(productRepository, never()).searchByName(anyString(), any(Pageable.class));
    }

    @Test
    void createProduct_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product saved = productService.createProduct(product);

        assertThat(saved).isEqualTo(product);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void deactivateProduct_ShouldSetActiveFalse() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        productService.deactivateProduct(1L);

        assertThat(existing.getActive()).isFalse();
        verify(productRepository).save(existing);
    }

    @Test
    void reactivateProduct_ShouldSetActiveTrue() {
        Product existing = new Product();
        existing.setId(2L);
        existing.setActive(false);

        when(productRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        productService.reactivateProduct(2L);

        assertThat(existing.getActive()).isTrue();
        verify(productRepository).save(existing);
    }
}