package com.tableorder.staffcall.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_calls")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffCall {

    public static final String PENDING = "PENDING";
    public static final String ATTENDED = "ATTENDED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "table_number", nullable = false)
    private int tableNumber;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(nullable = false, length = 50)
    private String reason;

    @Column(length = 200)
    private String message;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = PENDING;

    @Column(name = "called_at", nullable = false)
    @Builder.Default
    private LocalDateTime calledAt = LocalDateTime.now();

    @Column(name = "attended_at")
    private LocalDateTime attendedAt;
}
