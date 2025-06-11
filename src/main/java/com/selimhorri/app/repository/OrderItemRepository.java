package com.selimhorri.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.selimhorri.app.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByIsActiveTrue();

}
