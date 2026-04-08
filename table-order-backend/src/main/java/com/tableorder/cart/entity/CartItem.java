package com.tableorder.cart.entity;

import com.tableorder.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "cart_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cart_id", nullable = false) private Cart cart;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "menu_id", nullable = false) private Menu menu;
    @Column(name = "customer_profile_id") private Long customerProfileId;
    @Column(name = "device_id", nullable = false) private String deviceId;
    @Column(nullable = false) private int quantity;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
