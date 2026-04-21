package com.example.epochshop.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epochshop.dto.CartItemRequest;
import com.example.epochshop.dto.CartResponse;
import com.example.epochshop.entity.Cart;
import com.example.epochshop.entity.CartItem;
import com.example.epochshop.entity.Product;
import com.example.epochshop.entity.User;
import com.example.epochshop.repository.CartItemRepository;
import com.example.epochshop.repository.CartRepository;
import com.example.epochshop.repository.ProductRepository;
import com.example.epochshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public CartResponse addItemToCart(CartItemRequest request, String idempotentKey) {
        // 1. 檢查冪等鍵是否已存在
        if (cartItemRepository.existsByIdempotentKey(idempotentKey)) {
            return getCart();  // 已處理過，直接返回當前購物車內容
        }

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(product.getPrice());
            newItem.setIdempotentKey(idempotentKey);  // ✅ 寫入冪等鍵
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse getCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Long itemId, Integer quantity) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
        }
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long itemId) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Access denied");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    private CartResponse buildCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setItems(cart.getItems().stream()
                .map(item -> {
                    CartResponse.CartItemResponse dto = new CartResponse.CartItemResponse();
                    dto.setItemId(item.getId());
                    dto.setProductId(item.getProduct().getId());
                    dto.setProductName(item.getProduct().getName());
                    dto.setUnitPrice(item.getUnitPrice());
                    dto.setQuantity(item.getQuantity());
                    dto.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    return dto;
                })
                .collect(Collectors.toList()));

        BigDecimal total = response.getItems().stream()
                .map(CartResponse.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalAmount(total);
        return response;
    }
}