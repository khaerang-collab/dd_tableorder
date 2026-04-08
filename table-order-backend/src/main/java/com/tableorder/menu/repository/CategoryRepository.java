package com.tableorder.menu.repository;

import com.tableorder.menu.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByStoreIdOrderByDisplayOrder(Long storeId);
    int countByStoreId(Long storeId);
}
