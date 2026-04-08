package com.tableorder.recommend.service;

import com.tableorder.menu.entity.Menu;
import com.tableorder.menu.repository.MenuRepository;
import com.tableorder.order.entity.Order;
import com.tableorder.order.entity.OrderItem;
import com.tableorder.order.repository.OrderRepository;
import com.tableorder.recommend.dto.MenuRecommendResponse;
import com.tableorder.recommend.entity.MenuPairing;
import com.tableorder.recommend.repository.MenuPairingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final MenuPairingRepository pairingRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;

    // 페어링 추천: 해당 메뉴와 함께 주문된 메뉴 Top 3
    public List<MenuRecommendResponse> getPairings(Long menuId) {
        List<MenuPairing> pairings = pairingRepository.findByMenuIdOrderByPairCountDesc(menuId);
        return pairings.stream().limit(3).map(p -> {
            Menu menu = menuRepository.findById(p.getPairedMenuId()).orElse(null);
            if (menu == null) return null;
            return new MenuRecommendResponse(menu.getId(), menu.getName(), menu.getPrice(),
                    menu.getImageUrl(), p.getPairCount());
        }).filter(r -> r != null).toList();
    }

    // 주문 완료 시 페어링 데이터 업데이트
    @Transactional
    public void updatePairings(Long storeId, List<OrderItem> items) {
        List<Long> menuIds = items.stream().map(OrderItem::getMenuId).filter(id -> id != null).toList();
        for (int i = 0; i < menuIds.size(); i++) {
            for (int j = i + 1; j < menuIds.size(); j++) {
                upsertPairing(storeId, menuIds.get(i), menuIds.get(j));
                upsertPairing(storeId, menuIds.get(j), menuIds.get(i));
            }
        }
    }

    private void upsertPairing(Long storeId, Long menuId, Long pairedMenuId) {
        MenuPairing pairing = pairingRepository.findByStoreIdAndMenuIdAndPairedMenuId(storeId, menuId, pairedMenuId)
                .orElseGet(() -> MenuPairing.builder().storeId(storeId).menuId(menuId).pairedMenuId(pairedMenuId).build());
        pairing.setPairCount(pairing.getPairCount() + 1);
        pairingRepository.save(pairing);
    }
}
