package com.tableorder.recommend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "menu_pairings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuPairing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "store_id", nullable = false) private Long storeId;
    @Column(name = "menu_id", nullable = false) private Long menuId;
    @Column(name = "paired_menu_id", nullable = false) private Long pairedMenuId;
    @Column(name = "pair_count", nullable = false) @Builder.Default private int pairCount = 0;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    @PrePersist @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
