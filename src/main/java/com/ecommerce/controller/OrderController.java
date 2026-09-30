package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.BuyNowRequest;
import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.OrderDto;
import com.ecommerce.entity.Customer;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders & Checkout", description = "Endpoints for buying products and managing orders")
@SecurityRequirement(name = "BearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @PostMapping("/checkout")
    @Operation(
            summary = "Checkout shopping cart (Buy cart products)",
            description = "Converts all items in the customer's current cart into a placed Order, checks and decrements inventory, and empties the cart."
    )
    public ResponseEntity<ApiResponse<OrderDto>> checkout(@Valid @RequestBody CheckoutRequest request) {
        Customer customer = authService.getCurrentCustomer();
        OrderDto order = orderService.checkout(customer, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully from cart items!", order));
    }

    @PostMapping("/buy-now")
    @Operation(
            summary = "Buy a product directly (Instant Purchase)",
            description = "Directly buys a specific product with specified quantity without requiring it to be placed in cart first."
    )
    public ResponseEntity<ApiResponse<OrderDto>> buyNow(@Valid @RequestBody BuyNowRequest request) {
        Customer customer = authService.getCurrentCustomer();
        OrderDto order = orderService.buyNow(customer, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product purchased successfully!", order));
    }

    @GetMapping
    @Operation(summary = "Get order history", description = "Retrieves all past orders placed by the authenticated customer.")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getCustomerOrders() {
        Customer customer = authService.getCurrentCustomer();
        List<OrderDto> orders = orderService.getCustomerOrders(customer);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID", description = "Retrieves specific order details including line items and status.")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long id
    ) {
        Customer customer = authService.getCurrentCustomer();
        OrderDto order = orderService.getOrderById(customer, id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}
