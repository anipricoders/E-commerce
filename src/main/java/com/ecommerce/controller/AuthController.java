package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.AuthRequest;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.entity.Customer;
import com.ecommerce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Customer registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new customer", description = "Creates a new customer account and returns a JWT token.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login customer", description = "Authenticates customer credentials and returns a JWT Bearer token.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated customer", description = "Returns profile details of the authenticated customer.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentCustomer() {
        Customer customer = authService.getCurrentCustomer();
        Map<String, Object> details = Map.of(
                "id", customer.getId(),
                "fullName", customer.getFullName(),
                "email", customer.getEmail(),
                "phone", customer.getPhone() != null ? customer.getPhone() : "",
                "address", customer.getAddress() != null ? customer.getAddress() : "",
                "role", customer.getRole().name(),
                "createdAt", customer.getCreatedAt()
        );
        return ResponseEntity.ok(ApiResponse.success(details));
    }
}
