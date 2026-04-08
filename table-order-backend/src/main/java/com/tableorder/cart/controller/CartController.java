package com.tableorder.cart.controller;

import com.tableorder.cart.dto.AddCartItemRequest;
import com.tableorder.cart.dto.CartItemResponse;
import com.tableorder.cart.dto.CartResponse;
import com.tableorder.cart.dto.UpdateCartItemRequest;
import com.tableorder.cart.service.CartService;
import com.tableorder.cart.websocket.CartWebSocketHandler;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/sessions/{sessionId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartWebSocketHandler webSocketHandler;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@PathVariable Long sessionId) {
        return ResponseEntity.ok(cartService.getCart(sessionId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long sessionId,
                                                 @Valid @RequestBody AddCartItemRequest request,
                                                 @AuthenticationPrincipal Claims claims) {
        Long profileId = claims.get("profileId") != null ? ((Number) claims.get("profileId")).longValue() : null;
        String deviceId = claims.get("deviceId") != null ? claims.get("deviceId").toString() : "unknown";
        CartResponse cart = cartService.addItem(sessionId, request.menuId(), request.quantity(), profileId, deviceId);
        String menuName = cart.items().stream()
                .filter(i -> i.menuId().equals(request.menuId()))
                .findFirst().map(CartItemResponse::menuName).orElse("메뉴");
        webSocketHandler.broadcast(sessionId, "CART_ITEM_ADDED",
                Map.of("menuId", request.menuId(), "profileId", profileId != null ? profileId : 0, "menuName", menuName));
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long sessionId, @PathVariable Long itemId,
                                                    @Valid @RequestBody UpdateCartItemRequest request,
                                                    @AuthenticationPrincipal Claims claims) {
        Long profileId = claims.get("profileId") != null ? ((Number) claims.get("profileId")).longValue() : null;
        CartResponse cart = cartService.updateItemQuantity(itemId, request.quantity(), sessionId);
        String menuName = cart.items().stream()
                .filter(i -> i.id().equals(itemId))
                .findFirst().map(CartItemResponse::menuName).orElse("메뉴");
        webSocketHandler.broadcast(sessionId, "CART_ITEM_UPDATED",
                Map.of("itemId", itemId, "profileId", profileId != null ? profileId : 0, "menuName", menuName));
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long sessionId, @PathVariable Long itemId,
                                                    @AuthenticationPrincipal Claims claims) {
        Long profileId = claims.get("profileId") != null ? ((Number) claims.get("profileId")).longValue() : null;
        String menuName = cartService.getCart(sessionId).items().stream()
                .filter(i -> i.id().equals(itemId))
                .findFirst().map(CartItemResponse::menuName).orElse("메뉴");
        CartResponse cart = cartService.removeItem(itemId, sessionId);
        webSocketHandler.broadcast(sessionId, "CART_ITEM_REMOVED",
                Map.of("itemId", itemId, "profileId", profileId != null ? profileId : 0, "menuName", menuName));
        return ResponseEntity.ok(cart);
    }
}
