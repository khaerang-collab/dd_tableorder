package com.tableorder.support;

import com.tableorder.auth.entity.AdminUser;
import com.tableorder.auth.repository.AdminUserRepository;
import com.tableorder.cart.entity.Cart;
import com.tableorder.cart.entity.CartItem;
import com.tableorder.cart.repository.CartItemRepository;
import com.tableorder.cart.repository.CartRepository;
import com.tableorder.customer.entity.CustomerProfile;
import com.tableorder.customer.repository.CustomerProfileRepository;
import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.menu.repository.CategoryRepository;
import com.tableorder.menu.repository.MenuRepository;
import com.tableorder.order.entity.Order;
import com.tableorder.order.entity.OrderItem;
import com.tableorder.order.repository.OrderRepository;
import com.tableorder.store.entity.Store;
import com.tableorder.store.repository.StoreRepository;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import com.tableorder.table.repository.TableRepository;
import com.tableorder.table.repository.TableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataFactory {

    private final StoreRepository storeRepository;
    private final AdminUserRepository adminUserRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final TableRepository tableRepository;
    private final TableSessionRepository sessionRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public Store createStore(String name) {
        return storeRepository.save(Store.builder()
                .name(name)
                .address("서울시 강남구")
                .phone("02-1234-5678")
                .build());
    }

    public AdminUser createAdminUser(Long storeId, String username, String rawPassword) {
        return adminUserRepository.save(AdminUser.builder()
                .storeId(storeId)
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build());
    }

    public Category createCategory(Store store, String name, int displayOrder) {
        return categoryRepository.save(Category.builder()
                .store(store)
                .name(name)
                .displayOrder(displayOrder)
                .build());
    }

    public Menu createMenu(Category category, String name, int price) {
        return menuRepository.save(Menu.builder()
                .category(category)
                .name(name)
                .price(price)
                .description(name + " 설명")
                .displayOrder(0)
                .isAvailable(true)
                .build());
    }

    public Menu createUnavailableMenu(Category category, String name, int price) {
        return menuRepository.save(Menu.builder()
                .category(category)
                .name(name)
                .price(price)
                .isAvailable(false)
                .build());
    }

    public RestaurantTable createTable(Store store, int tableNumber) {
        return tableRepository.save(RestaurantTable.builder()
                .store(store)
                .tableNumber(tableNumber)
                .qrCodeUrl("http://test.com/customer?storeId=" + store.getId() + "&table=" + tableNumber)
                .build());
    }

    public TableSession createActiveSession(RestaurantTable table) {
        return sessionRepository.save(TableSession.builder()
                .table(table)
                .status("ACTIVE")
                .activeUserCount(1)
                .startedAt(LocalDateTime.now())
                .build());
    }

    public CustomerProfile createCustomerProfile(Long kakaoId, String nickname) {
        return customerProfileRepository.save(CustomerProfile.builder()
                .kakaoId(kakaoId)
                .nickname(nickname)
                .visitCount(1)
                .lastVisitAt(LocalDateTime.now())
                .build());
    }

    public Cart createCart(Long sessionId) {
        return cartRepository.save(Cart.builder()
                .tableSessionId(sessionId)
                .build());
    }

    public CartItem createCartItem(Cart cart, Menu menu, String deviceId, int quantity) {
        return cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .menu(menu)
                .deviceId(deviceId)
                .quantity(quantity)
                .build());
    }

    public CartItem createCartItemWithProfile(Cart cart, Menu menu, Long profileId, String deviceId, int quantity) {
        return cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .menu(menu)
                .customerProfileId(profileId)
                .deviceId(deviceId)
                .quantity(quantity)
                .build());
    }

    public Order createOrder(Long sessionId, String status, int totalAmount, List<OrderItem> items) {
        Order order = Order.builder()
                .tableSessionId(sessionId)
                .orderNumber("20260408-120000-" + String.format("%04d", (int) (Math.random() * 10000)))
                .totalAmount(totalAmount)
                .status(status)
                .build();
        for (OrderItem item : items) {
            item.setOrder(order);
            order.getItems().add(item);
        }
        return orderRepository.save(order);
    }

    public OrderItem buildOrderItem(Long menuId, String menuName, int quantity, int unitPrice) {
        return OrderItem.builder()
                .menuId(menuId)
                .menuName(menuName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }
}
