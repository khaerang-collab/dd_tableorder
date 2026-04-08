package com.tableorder.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.order.entity.Order;
import com.tableorder.order.entity.OrderHistory;
import com.tableorder.order.repository.OrderHistoryRepository;
import com.tableorder.order.repository.OrderRepository;
import com.tableorder.table.entity.TableSession;
import com.tableorder.table.repository.TableSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository historyRepository;
    private final TableSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void archiveOrders(Long sessionId, Long storeId, Long tableId, int tableNumber) {
        List<Order> orders = orderRepository.findByTableSessionIdOrderByCreatedAtAsc(sessionId);
        if (orders.isEmpty()) return;

        TableSession session = sessionRepository.findById(sessionId).orElse(null);
        int totalAmount = orders.stream().mapToInt(Order::getTotalAmount).sum();

        try {
            String json = objectMapper.writeValueAsString(orders.stream().map(o ->
                    java.util.Map.of(
                            "orderNumber", o.getOrderNumber(),
                            "totalAmount", o.getTotalAmount(),
                            "status", o.getStatus(),
                            "createdAt", o.getCreatedAt().toString(),
                            "items", o.getItems().stream().map(i ->
                                    java.util.Map.of("menuName", i.getMenuName(),
                                            "quantity", i.getQuantity(),
                                            "unitPrice", i.getUnitPrice())
                            ).toList()
                    )).toList());

            historyRepository.save(OrderHistory.builder()
                    .storeId(storeId).tableId(tableId).tableNumber(tableNumber)
                    .sessionId(sessionId).orderDataJson(json).totalAmount(totalAmount)
                    .sessionStartedAt(session != null ? session.getStartedAt() : null)
                    .build());
        } catch (Exception e) {
            log.error("Failed to archive orders for session {}", sessionId, e);
        }
    }

    public List<OrderHistory> getHistory(Long storeId, Long tableId) {
        if (tableId != null) return historyRepository.findByStoreIdAndTableIdOrderByCompletedAtDesc(storeId, tableId);
        return historyRepository.findByStoreIdOrderByCompletedAtDesc(storeId);
    }
}
