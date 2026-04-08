package com.tableorder.cart.service;

import com.tableorder.cart.dto.CartResponse;
import com.tableorder.cart.entity.Cart;
import com.tableorder.cart.entity.CartItem;
import com.tableorder.cart.repository.CartItemRepository;
import com.tableorder.cart.repository.CartRepository;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.customer.entity.CustomerProfile;
import com.tableorder.customer.repository.CustomerProfileRepository;
import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.menu.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private CustomerProfileRepository customerProfileRepository;

    private Menu menu;
    private Cart cart;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("메인").build();
        menu = Menu.builder().id(1L).category(category).name("불고기").price(15000).isAvailable(true).build();
        cart = Cart.builder().id(1L).tableSessionId(1L).build();
    }

    // UT-CART-001: 빈 장바구니 조회
    @Test
    @DisplayName("장바구니가 없으면 빈 CartResponse를 반환한다")
    void getEmptyCart() {
        when(cartRepository.findByTableSessionId(1L)).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart(1L);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalAmount()).isZero();
    }

    // UT-CART-002: 장바구니에 아이템 추가
    @Test
    @DisplayName("장바구니에 메뉴를 추가하면 CartItem이 생성된다")
    void addItemToCart() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(cartRepository.findByTableSessionId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.findByCartIdOrderByCreatedAtAsc(1L)).thenReturn(new ArrayList<>());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
            CartItem item = inv.getArgument(0);
            item.setId(1L);
            return item;
        });

        // 새 Cart 생성 후 아이템 추가
        CartResponse response = cartService.addItem(1L, 1L, 2, null, "device-1");

        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    // UT-CART-003: 동일 기기+메뉴 추가 시 수량 누적 (UPSERT)
    @Test
    @DisplayName("동일 기기에서 같은 메뉴를 추가하면 기존 아이템의 수량이 증가한다")
    void addItemUpsert() {
        CartItem existing = CartItem.builder()
                .id(1L).cart(cart).menu(menu).deviceId("device-1").quantity(2).build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(cartRepository.findByTableSessionId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(existing));

        cartService.addItem(1L, 1L, 3, null, "device-1");

        assertThat(existing.getQuantity()).isEqualTo(5); // 2 + 3
        verify(cartItemRepository, never()).save(any(CartItem.class)); // 새 아이템 저장 안됨
    }

    // UT-CART-004: 다른 기기에서 동일 메뉴 추가 시 별도 아이템
    @Test
    @DisplayName("다른 기기에서 동일 메뉴를 추가하면 새로운 CartItem이 생성된다")
    void addItemDifferentDevice() {
        CartItem existing = CartItem.builder()
                .id(1L).cart(cart).menu(menu).deviceId("device-1").quantity(2).build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(cartRepository.findByTableSessionId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.addItem(1L, 1L, 1, null, "device-2");

        verify(cartItemRepository).save(any(CartItem.class)); // 새 아이템 저장됨
        assertThat(existing.getQuantity()).isEqualTo(2); // 기존 수량 유지
    }

    // UT-CART-005: 아이템 수량 변경
    @Test
    @DisplayName("장바구니 아이템의 수량을 변경할 수 있다")
    void updateItemQuantity() {
        CartItem item = CartItem.builder()
                .id(1L).cart(cart).menu(menu).deviceId("device-1").quantity(3).build();

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartRepository.findByTableSessionId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(item));

        cartService.updateItemQuantity(1L, 5, 1L);

        assertThat(item.getQuantity()).isEqualTo(5);
    }

    // UT-CART-006: 아이템 삭제
    @Test
    @DisplayName("장바구니 아이템을 삭제할 수 있다")
    void removeItem() {
        CartItem item = CartItem.builder()
                .id(1L).cart(cart).menu(menu).deviceId("device-1").quantity(2).build();

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartRepository.findByTableSessionId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByCreatedAtAsc(1L)).thenReturn(new ArrayList<>());

        cartService.removeItem(1L, 1L);

        verify(cartItemRepository).delete(item);
    }

    // UT-CART-007: 존재하지 않는 메뉴 추가 시도
    @Test
    @DisplayName("존재하지 않는 메뉴를 장바구니에 추가하면 NotFoundException이 발생한다")
    void addNonExistentMenu() {
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(1L, 999L, 1, null, "device-1"))
                .isInstanceOf(NotFoundException.class);
    }

    // UT-CART-008: 장바구니 전체 삭제
    @Test
    @DisplayName("장바구니를 전체 비울 수 있다")
    void clearAll() {
        when(cartRepository.findByTableSessionId(1L)).thenReturn(Optional.of(cart));

        cartService.clearAll(1L);

        verify(cartItemRepository).deleteByCartId(1L);
    }
}
