package com.tableorder.order.service;

import com.tableorder.cart.entity.CartItem;
import com.tableorder.cart.service.CartService;
import com.tableorder.cart.websocket.CartWebSocketHandler;
import com.tableorder.common.exception.*;
import com.tableorder.customer.service.CustomerProfileService;
import com.tableorder.order.dto.*;
import com.tableorder.order.entity.Order;
import com.tableorder.order.entity.OrderItem;
import com.tableorder.order.repository.OrderRepository;
import com.tableorder.order.sse.OrderSseService;
import com.tableorder.recommend.service.RecommendService;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import com.tableorder.table.repository.TableRepository;
import com.tableorder.table.repository.TableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartWebSocketHandler cartWebSocketHandler;
    private final OrderSseService orderSseService;
    private final TableSessionRepository sessionRepository;
    private final TableRepository tableRepository;
    private final CustomerProfileService customerProfileService;
    private final RecommendService recommendService;

    @Transactional
    public OrderResponse createOrder(Long sessionId) {
        List<CartItem> cartItems = cartService.getCartItems(sessionId);
        if (cartItems.isEmpty()) throw new EmptyCartException();

        TableSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("세션을 찾을 수 없습니다"));
        RestaurantTable table = session.getTable();

        String orderNumber = generateOrderNumber();
        Order order = Order.builder()
                .tableSessionId(sessionId).orderNumber(orderNumber).build();

        int total = 0;
        for (CartItem ci : cartItems) {
            // 주문자 닉네임 조회
            String nickname = null;
            if (ci.getCustomerProfileId() != null) {
                try { nickname = customerProfileService.getById(ci.getCustomerProfileId()).getNickname(); }
                catch (Exception e) { /* ignore */ }
            }

            OrderItem oi = OrderItem.builder()
                    .order(order).menuId(ci.getMenu().getId())
                    .menuName(ci.getMenu().getName())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getMenu().getPrice())
                    .customerProfileId(ci.getCustomerProfileId())
                    .customerNickname(nickname)
                    .build();
            order.getItems().add(oi);
            total += ci.getQuantity() * ci.getMenu().getPrice();

            if (ci.getCustomerProfileId() != null) {
                customerProfileService.addOrderAmount(ci.getCustomerProfileId(), ci.getQuantity() * ci.getMenu().getPrice());
            }
        }
        order.setTotalAmount(total);
        orderRepository.save(order);
        cartService.clearAll(sessionId);

        // 페어링 데이터 업데이트
        try { recommendService.updatePairings(table.getStore().getId(), order.getItems()); }
        catch (Exception e) { /* 추천 업데이트 실패해도 주문은 정상 진행 */ }

        cartWebSocketHandler.broadcast(sessionId, "CART_CLEARED", Map.of("reason", "ORDER_PLACED"));
        orderSseService.publish(table.getStore().getId(), "NEW_ORDER", Map.of(
                "orderId", order.getId(), "orderNumber", orderNumber,
                "tableNumber", table.getTableNumber(), "totalAmount", total));

        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, Long storeId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다"));
        validateTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        orderSseService.publish(storeId, "ORDER_STATUS_CHANGED",
                Map.of("orderId", orderId, "newStatus", newStatus));
        return toResponse(order);
    }

    @Transactional
    public void deleteOrder(Long orderId, Long storeId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다"));
        orderRepository.delete(order);
        orderSseService.publish(storeId, "ORDER_DELETED", Map.of("orderId", orderId));
    }

    public List<OrderResponse> getOrdersBySession(Long sessionId) {
        return orderRepository.findByTableSessionIdOrderByCreatedAtAsc(sessionId)
                .stream().map(this::toResponse).toList();
    }

    public DashboardResponse getDashboard(Long storeId) {
        List<RestaurantTable> tables = tableRepository.findByStoreIdOrderByTableNumber(storeId);
        List<TableOrderSummary> summaries = tables.stream().map(table -> {
            TableSession session = sessionRepository.findByTableIdAndStatus(table.getId(), TableSession.ACTIVE).orElse(null);
            if (session == null)
                return new TableOrderSummary(table.getId(), table.getTableNumber(), null, 0, 0, List.of());

            List<Order> orders = orderRepository.findByTableSessionIdOrderByCreatedAtAsc(session.getId());
            int total = orders.stream().mapToInt(Order::getTotalAmount).sum();
            List<OrderResponse> recent = orders.stream().map(this::toResponse).toList();
            return new TableOrderSummary(table.getId(), table.getTableNumber(), session.getId(),
                    total, orders.size(), recent);
        }).toList();

        return new DashboardResponse(summaries);
    }

    private void validateTransition(String current, String next) {
        boolean valid = (Order.PENDING.equals(current) && Order.PREPARING.equals(next))
                || (Order.PREPARING.equals(current) && Order.COMPLETED.equals(next));
        if (!valid) throw new InvalidStatusTransitionException(
                String.format("%s → %s 상태 변경은 허용되지 않습니다", current, next));
    }

    private String generateOrderNumber() {
        String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
        String rand = String.format("%04d", new java.util.Random().nextInt(10000));
        return ts + "-" + rand;
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(i.getId(), i.getMenuName(), i.getQuantity(), i.getUnitPrice(), i.getCustomerProfileId(), i.getCustomerNickname()))
                .toList();
        return new OrderResponse(o.getId(), o.getOrderNumber(), o.getTotalAmount(), o.getStatus(), items, o.getCreatedAt());
    }
}
