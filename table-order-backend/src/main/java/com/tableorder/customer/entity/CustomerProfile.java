package com.tableorder.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "kakao_id", nullable = false, unique = true) private Long kakaoId;
    private String nickname;
    private String gender;
    @Column(name = "age_range") private String ageRange;
    @Column(name = "profile_image_url") private String profileImageUrl;
    @Column(name = "visit_count") @Builder.Default private int visitCount = 0;
    @Column(name = "total_order_amount") @Builder.Default private long totalOrderAmount = 0;
    @Column(name = "last_visit_at") private LocalDateTime lastVisitAt;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
