package com.example.marketplace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shop_orders")
public class ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerUserId;

    @Column(name = "seller_user_id", nullable = false)
    private Long sellerUserId;

    @Column(name = "buyer_display_name")
    private String buyerDisplayName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShopOrderStatus status;

    @Column(name = "total_rub", nullable = false)
    private Double totalRub;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShopOrderLine> lines = new ArrayList<>();
}
