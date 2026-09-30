package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(Customer customer);
    Optional<Cart> findByCustomerId(Long customerId);
}
