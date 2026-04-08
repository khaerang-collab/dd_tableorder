package com.tableorder.cart.controller;

import com.tableorder.cart.dto.AddCartItemRequest;
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
        Long profileId = claims.get("profileId", Long.class);
        String deviceId = claims.get("deviceId", String.class);
        CartResponse cart = cartService.addItem(sessionId, request.menuId(), request.quantity(), profileId, deviceId);
        webSocketHandler.broadcast(sessionId, "CART_ITEM_ADDED", Map.of("menuId", request.menuId()));
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long sessionId, @PathVariable Long itemId,
                                                    @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateItemQuantity(itemId, request.quantity(), sessionId);
        webSocketHandler.broadcast(sessionId, "CART_ITEM_UPDATED", Map.of("itemId", itemId));
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long sessionId, @PathVariable Long itemId) {
        CartResponse cart = cartService.removeItem(itemId, sessionId);
        webSocketHandler.broadcast(sessionId, "CART_ITEM_REMOVED", Map.of("itemId", itemId));
        return ResponseEntity.ok(cart);
    }
}
