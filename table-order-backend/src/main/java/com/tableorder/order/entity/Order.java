package com.tableorder.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    public static final String PENDING = "PENDING";
    public static final String PREPARING = "PREPARING";
    public static final String COMPLETED = "COMPLETED";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "table_session_id", nullable = false) private Long tableSessionId;
    @Column(name = "order_number", nullable = false, unique = true) private String orderNumber;
    @Column(name = "total_amount", nullable = false) private int totalAmount;
    @Column(nullable = false) @Builder.Default private String status = PENDING;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<OrderItem> items = new ArrayList<>();
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
