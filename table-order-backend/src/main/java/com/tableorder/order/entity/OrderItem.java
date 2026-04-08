package com.tableorder.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = false) private Order order;
    @Column(name = "menu_id") private Long menuId;
    @Column(name = "menu_name", nullable = false) private String menuName;
    @Column(nullable = false) private int quantity;
    @Column(name = "unit_price", nullable = false) private int unitPrice;
    @Column(name = "customer_profile_id") private Long customerProfileId;
    @Column(name = "customer_nickname") private String customerNickname;
    @Column(name = "created_at") private LocalDateTime createdAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
