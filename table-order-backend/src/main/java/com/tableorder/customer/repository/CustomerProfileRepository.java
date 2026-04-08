package com.tableorder.customer.repository;

import com.tableorder.customer.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByKakaoId(Long kakaoId);
}
