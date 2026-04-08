package com.tableorder.staffcall.repository;

import com.tableorder.staffcall.entity.StaffCall;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaffCallRepository extends JpaRepository<StaffCall, Long> {
    List<StaffCall> findByStoreIdAndStatusOrderByCreatedAtDesc(Long storeId, String status);
    boolean existsByTableIdAndStatusAndCreatedAtAfter(Long tableId, String status, java.time.LocalDateTime after);
}
