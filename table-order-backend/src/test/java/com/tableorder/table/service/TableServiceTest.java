package com.tableorder.table.service;

import com.tableorder.store.entity.Store;
import com.tableorder.store.service.StoreService;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import com.tableorder.table.repository.TableRepository;
import com.tableorder.table.repository.TableSessionRepository;
import com.tableorder.cart.repository.CartItemRepository;
import com.tableorder.cart.repository.CartRepository;
import com.tableorder.cart.entity.Cart;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.order.service.OrderHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @InjectMocks
    private TableService tableService;

    @Mock private TableRepository tableRepository;
    @Mock private StoreService storeService;

    private Store store;

    @BeforeEach
    void setUp() {
        store = Store.builder().id(1L).name("테스트 매장").build();
    }

    // UT-TBL-001: 테이블 생성
    @Test
    @DisplayName("테이블을 생성하면 QR 코드 URL이 자동 생성된다")
    void createTable() {
        when(storeService.getStore(1L)).thenReturn(store);
        when(tableRepository.save(any(RestaurantTable.class))).thenAnswer(inv -> {
            RestaurantTable t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        RestaurantTable result = tableService.createTable(1L, 5, "http://localhost:3000");

        assertThat(result.getQrCodeUrl()).isEqualTo("http://localhost:3000/customer?storeId=1&table=5");
        assertThat(result.getTableNumber()).isEqualTo(5);
    }

    // UT-TBL-002: 테이블 목록 조회
    @Test
    @DisplayName("매장의 모든 테이블을 테이블 번호 순으로 조회한다")
    void getTables() {
        RestaurantTable t1 = RestaurantTable.builder().id(1L).store(store).tableNumber(1).build();
        RestaurantTable t2 = RestaurantTable.builder().id(2L).store(store).tableNumber(2).build();
        RestaurantTable t3 = RestaurantTable.builder().id(3L).store(store).tableNumber(3).build();

        when(tableRepository.findByStoreIdOrderByTableNumber(1L)).thenReturn(List.of(t1, t2, t3));

        List<RestaurantTable> result = tableService.getTables(1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTableNumber()).isEqualTo(1);
    }

    // UT-TBL-003: 테이블 번호로 테이블 조회 실패
    @Test
    @DisplayName("존재하지 않는 테이블 번호로 조회하면 NotFoundException이 발생한다")
    void getTableNotFound() {
        when(tableRepository.findByStoreIdAndTableNumber(1L, 99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.getTableByStoreAndNumber(1L, 99))
                .isInstanceOf(NotFoundException.class);
    }
}

@ExtendWith(MockitoExtension.class)
class TableSessionServiceTest {

    @InjectMocks
    private TableSessionService sessionService;

    @Mock private TableSessionRepository sessionRepository;
    @Mock private TableRepository tableRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private OrderHistoryService orderHistoryService;

    private RestaurantTable table;

    @BeforeEach
    void setUp() {
        Store store = Store.builder().id(1L).name("테스트 매장").build();
        table = RestaurantTable.builder().id(1L).store(store).tableNumber(1).build();
    }

    // UT-TBL-004: 활성 세션 가져오기 (기존 존재)
    @Test
    @DisplayName("활성 세션이 이미 존재하면 기존 세션을 반환한다")
    void getExistingActiveSession() {
        TableSession existing = TableSession.builder().id(1L).table(table).status("ACTIVE").build();
        when(sessionRepository.findByTableIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(existing));

        TableSession result = sessionService.getOrCreateActiveSession(1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(sessionRepository, never()).save(any());
    }

    // UT-TBL-005: 활성 세션 새로 생성
    @Test
    @DisplayName("활성 세션이 없으면 새 세션을 생성한다")
    void createNewActiveSession() {
        when(sessionRepository.findByTableIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.empty());
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(sessionRepository.save(any(TableSession.class))).thenAnswer(inv -> {
            TableSession s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });

        TableSession result = sessionService.getOrCreateActiveSession(1L);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(sessionRepository).save(any(TableSession.class));
    }

    // UT-TBL-006: 세션 완료 처리
    @Test
    @DisplayName("세션을 완료 처리하면 상태가 COMPLETED로 변경되고 장바구니가 삭제된다")
    void completeSession() {
        TableSession session = TableSession.builder()
                .id(1L).table(table).status("ACTIVE").startedAt(LocalDateTime.now()).build();
        Cart cart = Cart.builder().id(1L).tableSessionId(1L).build();

        when(sessionRepository.findByTableIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(session));
        when(cartRepository.findByTableSessionId(1L)).thenReturn(Optional.of(cart));

        sessionService.completeSession(1L, 1L);

        assertThat(session.getStatus()).isEqualTo("COMPLETED");
        assertThat(session.getCompletedAt()).isNotNull();
        verify(orderHistoryService).archiveOrders(eq(1L), eq(1L), eq(1L), eq(1));
        verify(cartItemRepository).deleteByCartId(1L);
        verify(cartRepository).delete(cart);
    }
}
