package com.tableorder.cart.repository;

import com.tableorder.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartIdOrderByCreatedAtAsc(Long cartId);
    void deleteByCartId(Long cartId);
}
