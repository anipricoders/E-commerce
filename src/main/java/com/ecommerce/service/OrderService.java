package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Transactional
    public OrderDto checkout(Customer customer, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCart(customer);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout: Shopping cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(
                orderNumber,
                customer,
                BigDecimal.ZERO,
                OrderStatus.CONFIRMED,
                request.getShippingAddress() != null && !request.getShippingAddress().isBlank()
                        ? request.getShippingAddress()
                        : (customer.getAddress() != null ? customer.getAddress() : "Default Address"),
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD"
        );

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'. Available: " + product.getStockQuantity());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal subtotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    product.getName(),
                    cartItem.getUnitPrice(),
                    cartItem.getQuantity(),
                    subtotal
            );
            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Clear user's cart after successful checkout
        cartService.clearCart(customer);

        return mapToOrderDto(savedOrder);
    }

    @Transactional
    public OrderDto buyNow(Customer customer, BuyNowRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'. Available: " + product.getStockQuantity());
        }

        // Deduct stock
        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(
                orderNumber,
                customer,
                subtotal,
                OrderStatus.CONFIRMED,
                request.getShippingAddress() != null && !request.getShippingAddress().isBlank()
                        ? request.getShippingAddress()
                        : (customer.getAddress() != null ? customer.getAddress() : "Default Address"),
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD"
        );

        OrderItem orderItem = new OrderItem(
                order,
                product,
                product.getName(),
                product.getPrice(),
                request.getQuantity(),
                subtotal
        );

        order.addItem(orderItem);
        Order savedOrder = orderRepository.save(order);

        return mapToOrderDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getCustomerOrders(Customer customer) {
        List<Order> orders = orderRepository.findByCustomerOrderByOrderDateDesc(customer);
        return orders.stream().map(this::mapToOrderDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Customer customer, Long orderId) {
        Order order = orderRepository.findByIdAndCustomer(orderId, customer)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToOrderDto(order);
    }

    private OrderDto mapToOrderDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getId(),
                        item.getProduct() != null ? item.getProduct().getId() : null,
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new OrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomer().getEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getPaymentMethod(),
                itemDtos,
                order.getOrderDate()
        );
    }
}
