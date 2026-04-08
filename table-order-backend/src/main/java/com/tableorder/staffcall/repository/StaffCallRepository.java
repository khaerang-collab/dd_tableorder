package com.tableorder.staffcall.repository;

import com.tableorder.staffcall.entity.StaffCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffCallRepository extends JpaRepository<StaffCall, Long> {
    List<StaffCall> findByStoreIdOrderByCalledAtDesc(Long storeId);
    List<StaffCall> findByStoreIdAndStatusOrderByCalledAtDesc(Long storeId, String status);
}
