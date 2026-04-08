package com.tableorder.order.repository;

import com.tableorder.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTableSessionIdOrderByCreatedAtAsc(Long sessionId);
}
