package com.tableorder.menu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "menus")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Menu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false) private int price;
    @Column(length = 500) private String description;
    @Column(name = "image_url") private String imageUrl;
    @Column(name = "badge_type") private String badgeType;
    @Column(name = "display_order") @Builder.Default private int displayOrder = 0;
    @Column(name = "is_available") @Builder.Default private boolean isAvailable = true;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
