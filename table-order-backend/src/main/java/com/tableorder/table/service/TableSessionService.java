package com.tableorder.table.service;

import com.tableorder.cart.repository.CartItemRepository;
import com.tableorder.cart.repository.CartRepository;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.order.service.OrderHistoryService;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import com.tableorder.table.repository.TableRepository;
import com.tableorder.table.repository.TableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TableSessionService {

    private final TableSessionRepository sessionRepository;
    private final TableRepository tableRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderHistoryService orderHistoryService;

    @Transactional
    public TableSession getOrCreateActiveSession(Long tableId) {
        return sessionRepository.findByTableIdAndStatus(tableId, TableSession.ACTIVE)
                .orElseGet(() -> {
                    RestaurantTable table = tableRepository.findById(tableId)
                            .orElseThrow(() -> new NotFoundException("테이블을 찾을 수 없습니다"));
                    TableSession session = TableSession.builder().table(table).build();
                    return sessionRepository.save(session);
                });
    }

    @Transactional
    public void completeSession(Long storeId, Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("테이블을 찾을 수 없습니다"));
        TableSession session = sessionRepository.findByTableIdAndStatus(tableId, TableSession.ACTIVE)
                .orElseThrow(() -> new NotFoundException("활성 세션을 찾을 수 없습니다"));

        orderHistoryService.archiveOrders(session.getId(), storeId, tableId, table.getTableNumber());

        cartRepository.findByTableSessionId(session.getId()).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.delete(cart);
        });

        session.setStatus(TableSession.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
    }

    @Transactional
    public void incrementUserCount(Long sessionId) {
        sessionRepository.findById(sessionId).ifPresent(s ->
                s.setActiveUserCount(s.getActiveUserCount() + 1));
    }

    @Transactional
    public void decrementUserCount(Long sessionId) {
        sessionRepository.findById(sessionId).ifPresent(s ->
                s.setActiveUserCount(Math.max(0, s.getActiveUserCount() - 1)));
    }
}
