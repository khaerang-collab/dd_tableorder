package com.tableorder.table.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "table_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TableSession {
    public static final String ACTIVE = "ACTIVE";
    public static final String COMPLETED = "COMPLETED";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;
    @Column(nullable = false) @Builder.Default private String status = ACTIVE;
    @Column(name = "active_user_count") @Builder.Default private int activeUserCount = 0;
    @Column(name = "started_at") private LocalDateTime startedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() { startedAt = LocalDateTime.now(); }
}
