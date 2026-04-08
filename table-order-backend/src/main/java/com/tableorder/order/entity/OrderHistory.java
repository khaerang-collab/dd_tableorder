package com.tableorder.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity @Table(name = "order_histories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "store_id", nullable = false) private Long storeId;
    @Column(name = "table_id", nullable = false) private Long tableId;
    @Column(name = "table_number", nullable = false) private int tableNumber;
    @Column(name = "session_id", nullable = false) private Long sessionId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "order_data_json", columnDefinition = "jsonb", nullable = false) private String orderDataJson;
    @Column(name = "total_amount", nullable = false) private int totalAmount;
    @Column(name = "session_started_at", nullable = false) private LocalDateTime sessionStartedAt;
    @Column(name = "completed_at", nullable = false) private LocalDateTime completedAt;

    @PrePersist protected void onCreate() { completedAt = LocalDateTime.now(); }
}
