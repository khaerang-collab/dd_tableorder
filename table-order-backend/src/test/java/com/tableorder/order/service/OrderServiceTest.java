package com.tableorder.order.service;

import com.tableorder.cart.entity.Cart;
import com.tableorder.cart.entity.CartItem;
import com.tableorder.cart.service.CartService;
import com.tableorder.cart.websocket.CartWebSocketHandler;
import com.tableorder.common.exception.EmptyCartException;
import com.tableorder.common.exception.InvalidStatusTransitionException;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.customer.service.CustomerProfileService;
import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.order.dto.DashboardResponse;
import com.tableorder.order.dto.OrderResponse;
import com.tableorder.order.entity.Order;
import com.tableorder.order.entity.OrderItem;
import com.tableorder.order.repository.OrderRepository;
import com.tableorder.order.sse.OrderSseService;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import com.tableorder.table.repository.TableRepository;
import com.tableorder.table.repository.TableSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock private OrderRepository orderRepository;
    @Mock private CartService cartService;
    @Mock private CartWebSocketHandler cartWebSocketHandler;
    @Mock private OrderSseService orderSseService;
    @Mock private TableSessionRepository sessionRepository;
    @Mock private TableRepository tableRepository;
    @Mock private CustomerProfileService customerProfileService;

    private Category category;
    private Menu menu1, menu2;
    private Cart cart;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("메인").build();
        menu1 = Menu.builder().id(1L).category(category).name("불고기").price(15000).build();
        menu2 = Menu.builder().id(2L).category(category).name("비빔밥").price(10000).build();
        cart = Cart.builder().id(1L).tableSessionId(1L).build();
    }

    // UT-ORD-001: 주문 생성 성공
    @Test
    @DisplayName("장바구니에 아이템이 있으면 주문을 성공적으로 생성한다")
    void createOrderSuccess() {
        CartItem item1 = CartItem.builder().id(1L).cart(cart).menu(menu1).deviceId("d1").quantity(2).build();
        CartItem item2 = CartItem.builder().id(2L).cart(cart).menu(menu2).deviceId("d1").quantity(1).build();

        TableSession session = TableSession.builder().id(1L).startedAt(LocalDateTime.now()).build();
        RestaurantTable table = RestaurantTable.builder().id(1L).tableNumber(1)
                .store(com.tableorder.store.entity.Store.builder().id(1L).build()).build();
        session.setTable(table);

        when(cartService.getCartItems(1L)).thenReturn(List.of(item1, item2));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponse response = orderService.createOrder(1L);

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.totalAmount()).isEqualTo(40000); // 15000*2 + 10000*1
        assertThat(response.orderNumber()).matches("\\d{8}-\\d{6}-\\d{4}");

        verify(cartService).clearAll(1L);
        verify(cartWebSocketHandler).broadcast(eq(1L), eq("CART_CLEARED"), any());
        verify(orderSseService).publish(eq(1L), eq("NEW_ORDER"), any());
    }

    // UT-ORD-002: 빈 장바구니로 주문 생성 시도
    @Test
    @DisplayName("빈 장바구니로 주문하면 EmptyCartException이 발생한다")
    void createOrderEmptyCart() {
        when(cartService.getCartItems(1L)).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> orderService.createOrder(1L))
                .isInstanceOf(EmptyCartException.class);
    }

    // UT-ORD-003: 주문 상태 변경 PENDING → PREPARING
    @Test
    @DisplayName("PENDING 상태 주문을 PREPARING으로 변경할 수 있다")
    void updateStatusPendingToPreparing() {
        Order order = Order.builder().id(1L).tableSessionId(1L)
                .orderNumber("20260408-120000-0001").totalAmount(15000).status("PENDING")
                .build();
        order.setItems(new ArrayList<>());

        TableSession session = TableSession.builder().id(1L).build();
        RestaurantTable table = RestaurantTable.builder().id(1L)
                .store(com.tableorder.store.entity.Store.builder().id(1L).build()).build();
        session.setTable(table);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        OrderResponse response = orderService.updateStatus(1L, 1L, "PREPARING");

        assertThat(response.status()).isEqualTo("PREPARING");
        verify(orderSseService).publish(eq(1L), eq("ORDER_STATUS_CHANGED"), any());
    }

    // UT-ORD-004: 주문 상태 변경 PREPARING → COMPLETED
    @Test
    @DisplayName("PREPARING 상태 주문을 COMPLETED로 변경할 수 있다")
    void updateStatusPreparingToCompleted() {
        Order order = Order.builder().id(1L).tableSessionId(1L)
                .orderNumber("20260408-120000-0001").totalAmount(15000).status("PREPARING")
                .build();
        order.setItems(new ArrayList<>());

        TableSession session = TableSession.builder().id(1L).build();
        RestaurantTable table = RestaurantTable.builder().id(1L)
                .store(com.tableorder.store.entity.Store.builder().id(1L).build()).build();
        session.setTable(table);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        OrderResponse response = orderService.updateStatus(1L, 1L, "COMPLETED");

        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    // UT-ORD-005: 잘못된 상태 전이 PENDING → COMPLETED
    @Test
    @DisplayName("PENDING에서 COMPLETED로 직접 전이하면 InvalidStatusTransitionException이 발생한다")
    void invalidTransitionPendingToCompleted() {
        Order order = Order.builder().id(1L).tableSessionId(1L)
                .status("PENDING").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, 1L, "COMPLETED"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // UT-ORD-006: 잘못된 상태 전이 COMPLETED → PENDING
    @Test
    @DisplayName("COMPLETED에서 PENDING으로 역방향 전이하면 InvalidStatusTransitionException이 발생한다")
    void invalidTransitionCompletedToPending() {
        Order order = Order.builder().id(1L).tableSessionId(1L)
                .status("COMPLETED").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, 1L, "PENDING"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // UT-ORD-007: 잘못된 상태 전이 PREPARING → PENDING
    @Test
    @DisplayName("PREPARING에서 PENDING으로 역방향 전이하면 InvalidStatusTransitionException이 발생한다")
    void invalidTransitionPreparingToPending() {
        Order order = Order.builder().id(1L).tableSessionId(1L)
                .status("PREPARING").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, 1L, "PENDING"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // UT-ORD-008: 주문 삭제
    @Test
    @DisplayName("주문을 성공적으로 삭제할 수 있다")
    void deleteOrder() {
        Order order = Order.builder().id(1L).tableSessionId(1L).build();
        TableSession session = TableSession.builder().id(1L).build();
        RestaurantTable table = RestaurantTable.builder().id(1L)
                .store(com.tableorder.store.entity.Store.builder().id(1L).build()).build();
        session.setTable(table);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        orderService.deleteOrder(1L, 1L);

        verify(orderRepository).delete(order);
        verify(orderSseService).publish(eq(1L), eq("ORDER_DELETED"), any());
    }

    // UT-ORD-009: 존재하지 않는 주문 상태 변경
    @Test
    @DisplayName("존재하지 않는 주문의 상태를 변경하면 NotFoundException이 발생한다")
    void updateStatusOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(999L, 1L, "PREPARING"))
                .isInstanceOf(NotFoundException.class);
    }

    // UT-ORD-010: 주문번호 형식 검증
    @Test
    @DisplayName("생성된 주문번호는 yyyyMMdd-HHmmss-XXXX 형식이다")
    void orderNumberFormat() {
        CartItem item = CartItem.builder().id(1L).cart(cart).menu(menu1).deviceId("d1").quantity(1).build();
        TableSession session = TableSession.builder().id(1L).startedAt(LocalDateTime.now()).build();
        RestaurantTable table = RestaurantTable.builder().id(1L).tableNumber(1)
                .store(com.tableorder.store.entity.Store.builder().id(1L).build()).build();
        session.setTable(table);

        when(cartService.getCartItems(1L)).thenReturn(List.of(item));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponse response = orderService.createOrder(1L);

        assertThat(response.orderNumber()).matches("\\d{8}-\\d{6}-\\d{4}");
    }
}
