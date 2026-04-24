package com.example.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shop_order_lines")
public class ShopOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private ShopOrder order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(nullable = false)
    private Integer qty;

    @Column(name = "line_total_rub", nullable = false)
    private Double lineTotalRub;
}
