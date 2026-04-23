package com.example.epochshop.service;

import com.example.epochshop.dto.OrderResponse;
import com.example.epochshop.entity.*;
import com.example.epochshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;
    private final String idempotentKey = "test-key-123";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("29900"));
        product.setStock(10);
        product.setVersion(0);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(product.getPrice());

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void createOrder_WithValidCart_ShouldSucceed() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.existsByIdempotentKey(idempotentKey)).thenReturn(false);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponse response = orderService.createOrder(idempotentKey);

        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("59800"));
        assertThat(cart.getItems()).isEmpty();
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void createOrder_WithDuplicateIdempotentKey_ShouldThrow() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.existsByIdempotentKey(idempotentKey)).thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(idempotentKey))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("訂單處理中，請勿重複提交");

        verify(cartRepository, never()).findByUser(any());
    }

    @Test
    void createOrder_WithEmptyCart_ShouldThrow() {
        cart.getItems().clear();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.existsByIdempotentKey(idempotentKey)).thenReturn(false);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(idempotentKey))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("購物車是空的");
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrow() {
        product.setStock(1);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.existsByIdempotentKey(idempotentKey)).thenReturn(false);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(idempotentKey))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("庫存不足");
    }
}