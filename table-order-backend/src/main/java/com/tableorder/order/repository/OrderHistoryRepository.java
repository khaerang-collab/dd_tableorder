package com.tableorder.order.repository;

import com.tableorder.order.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    List<OrderHistory> findByStoreIdOrderByCompletedAtDesc(Long storeId);
    List<OrderHistory> findByStoreIdAndTableIdOrderByCompletedAtDesc(Long storeId, Long tableId);
}
