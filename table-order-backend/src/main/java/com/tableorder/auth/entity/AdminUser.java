package com.tableorder.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "store_id", nullable = false) private Long storeId;
    @Column(nullable = false) private String username;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(name = "login_attempts") @Builder.Default private int loginAttempts = 0;
    @Column(name = "locked_until") private LocalDateTime lockedUntil;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
