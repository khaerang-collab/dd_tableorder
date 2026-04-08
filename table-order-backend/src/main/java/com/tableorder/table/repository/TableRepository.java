package com.tableorder.table.repository;

import com.tableorder.table.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByStoreIdAndTableNumber(Long storeId, Integer tableNumber);
    List<RestaurantTable> findByStoreIdOrderByTableNumber(Long storeId);
}
