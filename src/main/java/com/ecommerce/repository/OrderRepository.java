package com.ecommerce.repository;

import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdAndCustomer(Long id, Customer customer);
}
