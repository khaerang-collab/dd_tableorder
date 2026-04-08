package com.tableorder.recommend.repository;

import com.tableorder.recommend.entity.MenuPairing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MenuPairingRepository extends JpaRepository<MenuPairing, Long> {
    List<MenuPairing> findByMenuIdOrderByPairCountDesc(Long menuId);
    Optional<MenuPairing> findByStoreIdAndMenuIdAndPairedMenuId(Long storeId, Long menuId, Long pairedMenuId);
}
