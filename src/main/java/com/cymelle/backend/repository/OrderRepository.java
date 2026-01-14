package com.cymelle.backend.repository;

import com.cymelle.backend.model.Order;
import com.cymelle.backend.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
    Page<Order> findByUserEmail(String email, Pageable pageable);
    Page<Order> findByUserEmailAndStatus(String email, OrderStatus status, Pageable pageable);
}
