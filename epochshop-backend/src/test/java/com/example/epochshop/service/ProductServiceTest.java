package com.example.epochshop.service;

import com.example.epochshop.entity.Product;
import com.example.epochshop.entity.User;
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
import org.junit.jupiter.api.AfterEach;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.epochshop.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    @Test
    void searchProducts_WithBlankKeyword_ShouldCallFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(product));
        when(productRepository.findAllActive(any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> result = productService.searchProducts("   ", pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(productRepository, times(1)).findAllActive(pageable);
        verify(productRepository, never()).searchByName(anyString(), any(Pageable.class));
    }

    @Test
    void createProduct_ShouldSaveAndReturnProduct() {
        // 模拟当前用户
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("admin");
        User currentUser = new User();
        currentUser.setId(1L);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(currentUser));

        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product saved = productService.createProduct(product);

        assertThat(saved).isEqualTo(product);
        assertThat(saved.getSeller()).isEqualTo(currentUser); // 验证卖家被设置
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