package com.tableorder.promotion.repository;

import com.tableorder.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByStoreIdOrderByMinOrderAmountAsc(Long storeId);
    List<Promotion> findByStoreIdAndIsActiveTrueOrderByMinOrderAmountAsc(Long storeId);
}
