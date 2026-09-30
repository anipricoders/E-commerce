package com.ecommerce.service;

import com.ecommerce.config.JwtService;
import com.ecommerce.dto.AuthRequest;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Role;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.CustomerRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            CustomerRepository customerRepository,
            CartRepository cartRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        Customer customer = new Customer(
                request.getFullName().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                request.getAddress(),
                Role.ROLE_CUSTOMER
        );

        Customer savedCustomer = customerRepository.save(customer);

        // Initialize an empty cart for the customer
        Cart cart = new Cart(savedCustomer);
        cartRepository.save(cart);

        String token = jwtService.generateToken(savedCustomer);

        return new AuthResponse(
                token,
                savedCustomer.getId(),
                savedCustomer.getFullName(),
                savedCustomer.getEmail(),
                savedCustomer.getRole().name()
        );
    }

    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        String token = jwtService.generateToken(customer);

        return new AuthResponse(
                token,
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getRole().name()
        );
    }

    public Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadRequestException("Customer is not authenticated");
        }

        String email = authentication.getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current customer not found with email: " + email));
    }
}
