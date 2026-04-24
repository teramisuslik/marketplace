package com.example.marketplace.repository;

import com.example.marketplace.entity.ShopOrder;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    @EntityGraph(attributePaths = "lines")
    List<ShopOrder> findBySellerUserIdOrderByCreatedAtDesc(Long sellerUserId);

    @Query(
            "select coalesce(sum(o.totalRub), 0) from ShopOrder o where o.sellerUserId = :sid and o.createdAt >= :start and o.createdAt < :end")
    Double sumTotalRubForSellerBetween(
            @Param("sid") Long sellerUserId, @Param("start") Instant start, @Param("end") Instant end);

    @Query(
            "select count(o) from ShopOrder o where o.sellerUserId = :sid and o.createdAt >= :start and o.createdAt < :end")
    long countOrdersForSellerBetween(
            @Param("sid") Long sellerUserId, @Param("start") Instant start, @Param("end") Instant end);
}
