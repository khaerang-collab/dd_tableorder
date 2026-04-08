package com.tableorder.cart.service;

import com.tableorder.cart.dto.CartItemResponse;
import com.tableorder.cart.dto.CartResponse;
import com.tableorder.cart.entity.Cart;
import com.tableorder.cart.entity.CartItem;
import com.tableorder.cart.repository.CartItemRepository;
import com.tableorder.cart.repository.CartRepository;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.customer.entity.CustomerProfile;
import com.tableorder.customer.repository.CustomerProfileRepository;
import com.tableorder.menu.entity.Menu;
import com.tableorder.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final CustomerProfileRepository customerProfileRepository;

    public CartResponse getCart(Long sessionId) {
        Cart cart = cartRepository.findByTableSessionId(sessionId).orElse(null);
        if (cart == null) return new CartResponse(sessionId, List.of(), 0);

        List<CartItem> items = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        List<CartItemResponse> responses = items.stream().map(this::toResponse).toList();
        int total = items.stream().mapToInt(i -> i.getQuantity() * i.getMenu().getPrice()).sum();
        return new CartResponse(sessionId, responses, total);
    }

    @Transactional
    public CartResponse addItem(Long sessionId, Long menuId, int quantity, Long customerProfileId, String deviceId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다"));

        Cart cart = cartRepository.findByTableSessionId(sessionId)
                .orElseGet(() -> cartRepository.save(Cart.builder().tableSessionId(sessionId).build()));

        List<CartItem> existing = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        CartItem found = existing.stream()
                .filter(i -> i.getMenu().getId().equals(menuId) && i.getDeviceId().equals(deviceId))
                .findFirst().orElse(null);

        if (found != null) {
            found.setQuantity(found.getQuantity() + quantity);
        } else {
            cartItemRepository.save(CartItem.builder()
                    .cart(cart).menu(menu).customerProfileId(customerProfileId)
                    .deviceId(deviceId).quantity(quantity).build());
        }
        return getCart(sessionId);
    }

    @Transactional
    public CartResponse updateItemQuantity(Long cartItemId, int quantity, Long sessionId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("장바구니 항목을 찾을 수 없습니다"));
        item.setQuantity(quantity);
        return getCart(sessionId);
    }

    @Transactional
    public CartResponse removeItem(Long cartItemId, Long sessionId) {
        cartItemRepository.deleteById(cartItemId);
        return getCart(sessionId);
    }

    @Transactional
    public void clearAll(Long sessionId) {
        cartRepository.findByTableSessionId(sessionId).ifPresent(cart ->
                cartItemRepository.deleteByCartId(cart.getId()));
    }

    public List<CartItem> getCartItems(Long sessionId) {
        return cartRepository.findByTableSessionId(sessionId)
                .map(cart -> cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId()))
                .orElse(List.of());
    }

    private CartItemResponse toResponse(CartItem item) {
        Menu menu = item.getMenu();
        String nickname = null;
        String profileImage = null;
        if (item.getCustomerProfileId() != null) {
            CustomerProfile p = customerProfileRepository.findById(item.getCustomerProfileId()).orElse(null);
            if (p != null) { nickname = p.getNickname(); profileImage = p.getProfileImageUrl(); }
        }
        return new CartItemResponse(item.getId(), menu.getId(), menu.getName(), menu.getPrice(),
                menu.getImageUrl(), item.getCustomerProfileId(), nickname, profileImage,
                item.getDeviceId(), item.getQuantity(), item.getCreatedAt());
    }
}
