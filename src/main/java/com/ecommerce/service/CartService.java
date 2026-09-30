package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.CartItemDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart getOrCreateCart(Customer customer) {
        return cartRepository.findByCustomer(customer)
                .orElseGet(() -> cartRepository.save(new Cart(customer)));
    }

    @Transactional(readOnly = true)
    public CartDto getCustomerCartDto(Customer customer) {
        Cart cart = getOrCreateCart(customer);
        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto addToCart(Customer customer, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock (" + product.getStockQuantity() + ")");
        }

        Cart cart = getOrCreateCart(customer);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Total requested quantity (" + newQuantity + ") exceeds available stock (" + product.getStockQuantity() + ")");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setUnitPrice(product.getPrice());
        } else {
            CartItem newItem = new CartItem(cart, product, request.getQuantity(), product.getPrice());
            cart.addItem(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        return mapToCartDto(savedCart);
    }

    @Transactional
    public CartDto updateItemQuantity(Customer customer, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(customer);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (quantity <= 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            Product product = item.getProduct();
            if (product.getStockQuantity() < quantity) {
                throw new BadRequestException("Requested quantity (" + quantity + ") exceeds available stock (" + product.getStockQuantity() + ")");
            }
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        return mapToCartDto(savedCart);
    }

    @Transactional
    public CartDto removeItem(Customer customer, Long itemId) {
        Cart cart = getOrCreateCart(customer);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        cart.removeItem(item);
        cartItemRepository.delete(item);
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);
        return mapToCartDto(savedCart);
    }

    @Transactional
    public void clearCart(Customer customer) {
        Cart cart = getOrCreateCart(customer);
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    public CartDto mapToCartDto(Cart cart) {
        List<CartItemDto> itemDtos = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem item : cart.getItems()) {
            BigDecimal subtotal = item.getSubtotal();
            totalAmount = totalAmount.add(subtotal);
            totalItems += item.getQuantity();

            CartItemDto itemDto = new CartItemDto(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getCategory(),
                    item.getProduct().getImageUrl(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    subtotal
            );
            itemDtos.add(itemDto);
        }

        return new CartDto(
                cart.getId(),
                cart.getCustomer().getId(),
                itemDtos,
                totalItems,
                totalAmount
        );
    }
}
