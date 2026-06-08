package com.commerce.context_engine.repository;

import com.commerce.context_engine.core.KnowledgeEntry;
import com.commerce.context_engine.core.KnowledgeRepository;
import com.commerce.context_engine.domain.commerce.CommerceKnowledgeProperties;
import com.commerce.context_engine.domain.coupon.CouponKnowledgeProperties;
import com.commerce.context_engine.domain.inventory.InventoryKnowledgeProperties;
import com.commerce.context_engine.domain.payment.PaymentKnowledgeProperties;
import com.commerce.context_engine.domain.settlement.SettlementKnowledgeProperties;
import com.commerce.context_engine.domain.springcommerce.SpringCommerceKnowledgeProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class YamlKnowledgeRepository implements KnowledgeRepository {

    private final InventoryKnowledgeProperties inventory;
    private final PaymentKnowledgeProperties payment;
    private final SettlementKnowledgeProperties settlement;
    private final CouponKnowledgeProperties coupon;
    private final CommerceKnowledgeProperties commerce;
    private final SpringCommerceKnowledgeProperties springCommerce;

    /** 애플리케이션 기동 시 1회 빌드. YAML은 불변이므로 매 호출마다 재매핑하지 않는다. */
    private List<KnowledgeEntry> cache;

    @PostConstruct
    void buildCache() {
        cache = Stream.of(
                mapInventory(),
                mapPayment(),
                mapSettlement(),
                mapCoupon(),
                mapCommerce(),
                mapSpringCommerce()
        ).flatMap(List::stream).toList();
    }

    @Override
    public List<KnowledgeEntry> findAll() {
        return cache;
    }

    @Override
    public List<KnowledgeEntry> findByDomain(String domain) {
        return cache.stream()
                .filter(e -> domain.equals(e.domain()))
                .toList();
    }

    @Override
    public List<KnowledgeEntry> findByCategory(String domain, String category) {
        return cache.stream()
                .filter(e -> domain.equals(e.domain()) && category.equals(e.category()))
                .toList();
    }

    // ── mappers ──────────────────────────────────────────────────────────────

    private List<KnowledgeEntry> mapInventory() {
        return safeList(inventory.getItems()).stream()
                .map(item -> KnowledgeEntryMapper.fromSimple("inventory", item))
                .toList();
    }

    private List<KnowledgeEntry> mapPayment() {
        return safeList(payment.getItems()).stream()
                .map(item -> KnowledgeEntryMapper.fromSimple("payment", item))
                .toList();
    }

    private List<KnowledgeEntry> mapSettlement() {
        return safeList(settlement.getItems()).stream()
                .map(item -> KnowledgeEntryMapper.fromSimple("settlement", item))
                .toList();
    }

    private List<KnowledgeEntry> mapCoupon() {
        return safeList(coupon.getItems()).stream()
                .map(item -> KnowledgeEntryMapper.fromSimple("coupon", item))
                .toList();
    }

    private List<KnowledgeEntry> mapCommerce() {
        return safeList(commerce.getItems()).stream()
                .map(KnowledgeEntryMapper::from)
                .toList();
    }

    private List<KnowledgeEntry> mapSpringCommerce() {
        return safeList(springCommerce.getItems()).stream()
                .map(KnowledgeEntryMapper::from)
                .toList();
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
