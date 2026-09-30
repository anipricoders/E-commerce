package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.Customer;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    public CartController(CartService cartService, AuthService authService) {
        this.cartService = cartService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get current customer's cart", description = "Retrieves all items in the authenticated customer's shopping cart along with subtotal and total items.")
    public ResponseEntity<ApiResponse<CartDto>> getCart() {
        Customer customer = authService.getCurrentCustomer();
        CartDto cartDto = cartService.getCustomerCartDto(customer);
        return ResponseEntity.ok(ApiResponse.success(cartDto));
    }

    @PostMapping("/items")
    @Operation(summary = "Add product to cart", description = "Adds a product to the cart with specified quantity, or increments quantity if already in cart.")
    public ResponseEntity<ApiResponse<CartDto>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        Customer customer = authService.getCurrentCustomer();
        CartDto updatedCart = cartService.addToCart(customer, request);
        return ResponseEntity.ok(ApiResponse.success("Product added to cart", updatedCart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity", description = "Updates the quantity of a specific item in the customer's cart.")
    public ResponseEntity<ApiResponse<CartDto>> updateCartItem(
            @Parameter(description = "Cart Item ID") @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        Customer customer = authService.getCurrentCustomer();
        CartDto updatedCart = cartService.updateItemQuantity(customer, itemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated", updatedCart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes a specific item from the shopping cart.")
    public ResponseEntity<ApiResponse<CartDto>> removeFromCart(
            @Parameter(description = "Cart Item ID") @PathVariable Long itemId
    ) {
        Customer customer = authService.getCurrentCustomer();
        CartDto updatedCart = cartService.removeItem(customer, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", updatedCart));
    }

    @DeleteMapping
    @Operation(summary = "Clear shopping cart", description = "Removes all items from the customer's shopping cart.")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        Customer customer = authService.getCurrentCustomer();
        cartService.clearCart(customer);
        return ResponseEntity.ok(ApiResponse.success("Shopping cart cleared", null));
    }
}
