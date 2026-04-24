package com.example.marketplace.service;

import com.example.marketplace.DTO.*;
import com.example.marketplace.entity.Role;
import com.example.marketplace.entity.ShopOrder;
import com.example.marketplace.entity.ShopOrderLine;
import com.example.marketplace.entity.ShopOrderStatus;
import com.example.marketplace.repository.ShopOrderRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ShopOrderService {

    private final ShopOrderRepository shopOrderRepository;
    private final UserService userService;

    @Transactional
    public void recordCheckout(String authorization, RecordCheckoutRequest body) {
        Long buyerId = userService.getUserid(authorization);
        if (!buyerId.equals(body.getBuyerUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Несовпадение покупателя и токена");
        }
        if (body.getSellerGroups() == null || body.getSellerGroups().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пустой заказ");
        }
        ShopOrderStatus initial = initialStatus(body.getPaymentTiming());
        for (SellerCheckoutGroup group : body.getSellerGroups()) {
            if (group.getSellerUserId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не указан продавец");
            }
            if (group.getLines() == null || group.getLines().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пустые позиции заказа");
            }
            double total = group.getLines().stream()
                    .mapToDouble(l -> l.getLineTotalRub() != null ? l.getLineTotalRub() : 0.0)
                    .sum();
            ShopOrder order = ShopOrder.builder()
                    .buyerUserId(buyerId)
                    .sellerUserId(group.getSellerUserId())
                    .buyerDisplayName(trim(body.getBuyerDisplayName()))
                    .createdAt(Instant.now())
                    .status(initial)
                    .totalRub(total)
                    .build();
            for (CheckoutLineEnriched line : group.getLines()) {
                int qty = line.getQuantity() != null && line.getQuantity() > 0 ? line.getQuantity() : 1;
                ShopOrderLine ol = ShopOrderLine.builder()
                        .order(order)
                        .productId(line.getProductId())
                        .title(line.getProductName() != null ? line.getProductName() : "Товар")
                        .qty(qty)
                        .lineTotalRub(line.getLineTotalRub() != null ? line.getLineTotalRub() : 0.0)
                        .build();
                order.getLines().add(ol);
            }
            shopOrderRepository.save(order);
        }
    }

    public List<SellerOrderResponse> listSellerOrders(String authorization) {
        if (userService.getRole(authorization) != Role.SELLER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только для продавца");
        }
        Long sellerId = userService.getUserid(authorization);
        List<ShopOrder> orders = shopOrderRepository.findBySellerUserIdOrderByCreatedAtDesc(sellerId);
        List<SellerOrderResponse> out = new ArrayList<>();
        for (ShopOrder o : orders) {
            List<SellerOrderLineResponse> items = new ArrayList<>();
            for (ShopOrderLine line : o.getLines()) {
                items.add(new SellerOrderLineResponse(String.valueOf(line.getId()), line.getTitle(), line.getQty()));
            }
            String buyer =
                    o.getBuyerDisplayName() != null && !o.getBuyerDisplayName().isBlank()
                            ? o.getBuyerDisplayName()
                            : "Покупатель #" + o.getBuyerUserId();
            out.add(new SellerOrderResponse(
                    "SO-" + o.getId(),
                    o.getCreatedAt()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .toString(),
                    buyer,
                    "",
                    "",
                    o.getStatus().name(),
                    items,
                    o.getTotalRub() != null ? o.getTotalRub() : 0.0));
        }
        return out;
    }

    public SellerStatsResponse sellerStats(String authorization) {
        if (userService.getRole(authorization) != Role.SELLER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только для продавца");
        }
        Long sellerId = userService.getUserid(authorization);
        ZoneId z = ZoneId.of("Europe/Moscow");
        ZonedDateTime startZ = ZonedDateTime.now(z).toLocalDate().atStartOfDay(z);
        Instant start = startZ.toInstant();
        Instant end = startZ.plusDays(1).toInstant();
        Double revenue = shopOrderRepository.sumTotalRubForSellerBetween(sellerId, start, end);
        long count = shopOrderRepository.countOrdersForSellerBetween(sellerId, start, end);
        double rev = revenue != null ? revenue : 0.0;
        double avg = count > 0 ? rev / count : 0.0;
        return new SellerStatsResponse(rev, (int) count, avg);
    }

    private static ShopOrderStatus initialStatus(String paymentTiming) {
        if (paymentTiming != null && paymentTiming.equalsIgnoreCase("now")) {
            return ShopOrderStatus.assembly;
        }
        return ShopOrderStatus.awaiting_payment;
    }

    private static String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
