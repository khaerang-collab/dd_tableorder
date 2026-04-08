package com.tableorder.auth.repository;

import com.tableorder.auth.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByStoreIdAndUsername(Long storeId, String username);
}
