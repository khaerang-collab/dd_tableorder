package com.tableorder.menu.repository;

import com.tableorder.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByCategoryIdOrderByDisplayOrder(Long categoryId);

    @Query("SELECT m FROM Menu m JOIN m.category c WHERE c.store.id = :storeId AND m.isAvailable = true ORDER BY c.displayOrder, m.displayOrder")
    List<Menu> findAvailableByStoreId(Long storeId);

    int countByCategoryId(Long categoryId);
}
