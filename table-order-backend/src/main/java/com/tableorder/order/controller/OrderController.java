package com.tableorder.order.controller;

import com.tableorder.order.dto.*;
import com.tableorder.order.entity.OrderHistory;
import com.tableorder.order.service.OrderHistoryService;
import com.tableorder.order.service.OrderService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderHistoryService orderHistoryService;

    @PostMapping("/api/customer/sessions/{sessionId}/orders")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable Long sessionId) {
        return ResponseEntity.ok(orderService.createOrder(sessionId));
    }

    @GetMapping("/api/customer/sessions/{sessionId}/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(@PathVariable Long sessionId) {
        return ResponseEntity.ok(orderService.getOrdersBySession(sessionId));
    }

    @PutMapping("/api/admin/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long orderId,
                                                       @AuthenticationPrincipal Claims claims,
                                                       @Valid @RequestBody UpdateOrderStatusRequest request) {
        Long storeId = claims.get("storeId", Long.class);
        return ResponseEntity.ok(orderService.updateStatus(orderId, storeId, request.status()));
    }

    @DeleteMapping("/api/admin/orders/{orderId}")
    public ResponseEntity<Map<String, Boolean>> deleteOrder(@PathVariable Long orderId,
                                                             @AuthenticationPrincipal Claims claims) {
        Long storeId = claims.get("storeId", Long.class);
        orderService.deleteOrder(orderId, storeId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/api/admin/stores/{storeId}/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable Long storeId) {
        return ResponseEntity.ok(orderService.getDashboard(storeId));
    }

    @GetMapping("/api/admin/stores/{storeId}/orders/history")
    public ResponseEntity<List<OrderHistory>> getHistory(@PathVariable Long storeId,
                                                          @RequestParam(required = false) Long tableId) {
        return ResponseEntity.ok(orderHistoryService.getHistory(storeId, tableId));
    }
}
