package com.example.marketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.example.marketplace.DTO.*;
import com.example.marketplace.entity.Role;
import com.example.marketplace.entity.ShopOrder;
import com.example.marketplace.entity.ShopOrderLine;
import com.example.marketplace.entity.ShopOrderStatus;
import com.example.marketplace.repository.ShopOrderRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ShopOrderServiceTest {

    @Mock
    private ShopOrderRepository shopOrderRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ShopOrderService shopOrderService;

    private final String authHeader = "Bearer token";
    private final Long buyerId = 100L;
    private final Long sellerId1 = 200L;
    private final Long sellerId2 = 201L;

    @Test
    void recordCheckout_ShouldCreateOrdersForEachSeller_WhenPaymentNow() {
        // given
        when(userService.getUserid(authHeader)).thenReturn(buyerId);

        RecordCheckoutRequest request = new RecordCheckoutRequest();
        request.setBuyerUserId(buyerId);
        request.setBuyerDisplayName("   Покупатель Петя   ");
        request.setPaymentTiming("now");

        CheckoutLineEnriched line1 = new CheckoutLineEnriched();
        line1.setProductId(1L);
        line1.setProductName("Товар A");
        line1.setQuantity(2);
        line1.setLineTotalRub(200.0);

        CheckoutLineEnriched line2 = new CheckoutLineEnriched();
        line2.setProductId(2L);
        line2.setProductName("Товар B");
        line2.setQuantity(1);
        line2.setLineTotalRub(150.0);

        SellerCheckoutGroup group1 = new SellerCheckoutGroup();
        group1.setSellerUserId(sellerId1);
        group1.setLines(List.of(line1));

        SellerCheckoutGroup group2 = new SellerCheckoutGroup();
        group2.setSellerUserId(sellerId2);
        group2.setLines(List.of(line2));

        request.setSellerGroups(List.of(group1, group2));

        // when
        shopOrderService.recordCheckout(authHeader, request);

        // then
        verify(shopOrderRepository, times(2)).save(any(ShopOrder.class));
        verify(shopOrderRepository)
                .save(argThat(order -> order.getBuyerUserId().equals(buyerId)
                        && order.getSellerUserId().equals(sellerId1)
                        && order.getBuyerDisplayName().equals("Покупатель Петя")
                        && order.getStatus() == ShopOrderStatus.awaiting_payment
                        && order.getTotalRub().equals(200.0)
                        && order.getLines().size() == 1
                        && order.getLines().get(0).getProductId().equals(1L)
                        && order.getLines().get(0).getLineTotalRub().equals(200.0)));
        verify(shopOrderRepository)
                .save(argThat(order -> order.getSellerUserId().equals(sellerId2)
                        && order.getStatus() == ShopOrderStatus.awaiting_payment
                        && order.getTotalRub().equals(150.0)));
    }

    @Test
    void recordCheckout_ShouldCreateOrderWithAwaitingPayment_WhenPaymentOnDelivery() {
        // given
        when(userService.getUserid(authHeader)).thenReturn(buyerId);

        RecordCheckoutRequest request = new RecordCheckoutRequest();
        request.setBuyerUserId(buyerId);
        request.setBuyerDisplayName("Покупатель");
        request.setPaymentTiming("on_delivery");

        CheckoutLineEnriched line = new CheckoutLineEnriched();
        line.setProductId(1L);
        line.setQuantity(1);
        line.setLineTotalRub(100.0);

        SellerCheckoutGroup group = new SellerCheckoutGroup();
        group.setSellerUserId(sellerId1);
        group.setLines(List.of(line));
        request.setSellerGroups(List.of(group));

        // when
        shopOrderService.recordCheckout(authHeader, request);

        // then
        verify(shopOrderRepository).save(argThat(order -> order.getStatus() == ShopOrderStatus.awaiting_payment));
    }

    @Test
    void recordCheckout_ShouldThrowForbidden_WhenBuyerIdMismatch() {
        // given
        when(userService.getUserid(authHeader)).thenReturn(buyerId);

        RecordCheckoutRequest request = new RecordCheckoutRequest();
        request.setBuyerUserId(999L);

        // when/then
        assertThatThrownBy(() -> shopOrderService.recordCheckout(authHeader, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);
        verify(shopOrderRepository, never()).save(any());
    }

    @Test
    void recordCheckout_ShouldThrowBadRequest_WhenSellerGroupsEmpty() {
        // given
        when(userService.getUserid(authHeader)).thenReturn(buyerId);

        RecordCheckoutRequest request = new RecordCheckoutRequest();
        request.setBuyerUserId(buyerId);
        request.setSellerGroups(List.of());

        // when/then
        assertThatThrownBy(() -> shopOrderService.recordCheckout(authHeader, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void recordCheckout_ShouldThrowBadRequest_WhenSellerIdMissing() {
        // given
        when(userService.getUserid(authHeader)).thenReturn(buyerId);

        RecordCheckoutRequest request = new RecordCheckoutRequest();
        request.setBuyerUserId(buyerId);
        SellerCheckoutGroup group = new SellerCheckoutGroup();
        group.setSellerUserId(null);
        group.setLines(List.of(new CheckoutLineEnriched()));
        request.setSellerGroups(List.of(group));

        // when/then
        assertThatThrownBy(() -> shopOrderService.recordCheckout(authHeader, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listSellerOrders_ShouldReturnOrderResponses_WhenSellerRole() {
        // given
        when(userService.getRole(authHeader)).thenReturn(Role.SELLER);
        when(userService.getUserid(authHeader)).thenReturn(sellerId1);

        ShopOrder order = ShopOrder.builder()
                .id(1L)
                .buyerUserId(buyerId)
                .buyerDisplayName("Иван")
                .createdAt(Instant.now())
                .status(ShopOrderStatus.assembly)
                .totalRub(250.0)
                .build();
        ShopOrderLine line = ShopOrderLine.builder()
                .id(10L)
                .order(order)
                .productId(5L)
                .title("Книга")
                .qty(2)
                .lineTotalRub(250.0)
                .build();
        order.setLines(List.of(line));

        when(shopOrderRepository.findBySellerUserIdOrderByCreatedAtDesc(sellerId1))
                .thenReturn(List.of(order));

        // when
        List<SellerOrderResponse> responses = shopOrderService.listSellerOrders(authHeader);

        // then
        assertThat(responses).hasSize(1);
        SellerOrderResponse resp = responses.get(0);
        assertThat(resp.getId()).isEqualTo("SO-1");
        assertThat(resp.getBuyerName()).isEqualTo("Иван");
        assertThat(resp.getStatus()).isEqualTo("assembly");
        assertThat(resp.getTotalRub()).isEqualTo(250.0);
        assertThat(resp.getItems()).hasSize(1);
        assertThat(resp.getItems().get(0).getTitle()).isEqualTo("Книга");
        assertThat(resp.getItems().get(0).getQty()).isEqualTo(2);
    }

    @Test
    void listSellerOrders_ShouldUseDefaultBuyerName_WhenDisplayNameMissing() {
        // given
        when(userService.getRole(authHeader)).thenReturn(Role.SELLER);
        when(userService.getUserid(authHeader)).thenReturn(sellerId1);

        ShopOrder order = ShopOrder.builder()
                .id(2L)
                .buyerUserId(buyerId)
                .buyerDisplayName(null)
                .createdAt(Instant.now())
                .status(ShopOrderStatus.completed)
                .totalRub(100.0)
                .lines(List.of())
                .build();
        when(shopOrderRepository.findBySellerUserIdOrderByCreatedAtDesc(sellerId1))
                .thenReturn(List.of(order));

        // when
        List<SellerOrderResponse> responses = shopOrderService.listSellerOrders(authHeader);

        // then
        assertThat(responses.get(0).getBuyerName()).isEqualTo("Покупатель #100");
    }

    @Test
    void listSellerOrders_ShouldThrowForbidden_WhenNotSeller() {
        // given
        when(userService.getRole(authHeader)).thenReturn(Role.USER);

        // when/then
        assertThatThrownBy(() -> shopOrderService.listSellerOrders(authHeader))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sellerStats_ShouldReturnCorrectStats_ForToday() {
        // given
        when(userService.getRole(authHeader)).thenReturn(Role.SELLER);
        when(userService.getUserid(authHeader)).thenReturn(sellerId1);

        ZoneId z = ZoneId.of("Europe/Moscow");
        ZonedDateTime startZ = ZonedDateTime.now(z).toLocalDate().atStartOfDay(z);
        Instant start = startZ.toInstant();
        Instant end = startZ.plusDays(1).toInstant();

        when(shopOrderRepository.sumTotalRubForSellerBetween(eq(sellerId1), eq(start), eq(end)))
                .thenReturn(1500.0);
        when(shopOrderRepository.countOrdersForSellerBetween(eq(sellerId1), eq(start), eq(end)))
                .thenReturn(3L);

        // when
        SellerStatsResponse stats = shopOrderService.sellerStats(authHeader);

        // then
        assertThat(stats.getRevenueToday()).isEqualTo(1500.0);
        assertThat(stats.getOrdersCountToday()).isEqualTo(3);
        assertThat(stats.getAvgCheckToday()).isEqualTo(500.0);
    }

    @Test
    void sellerStats_ShouldHandleNullRevenue() {
        // given
        when(userService.getRole(authHeader)).thenReturn(Role.SELLER);
        when(userService.getUserid(authHeader)).thenReturn(sellerId1);

        ZoneId z = ZoneId.of("Europe/Moscow");
        ZonedDateTime startZ = ZonedDateTime.now(z).toLocalDate().atStartOfDay(z);
        Instant start = startZ.toInstant();
        Instant end = startZ.plusDays(1).toInstant();

        when(shopOrderRepository.sumTotalRubForSellerBetween(eq(sellerId1), eq(start), eq(end)))
                .thenReturn(null);
        when(shopOrderRepository.countOrdersForSellerBetween(eq(sellerId1), eq(start), eq(end)))
                .thenReturn(0L);

        // when
        SellerStatsResponse stats = shopOrderService.sellerStats(authHeader);

        // then
        assertThat(stats.getRevenueToday()).isEqualTo(0.0);
        assertThat(stats.getOrdersCountToday()).isEqualTo(0);
        assertThat(stats.getAvgCheckToday()).isEqualTo(0.0);
    }

    @Test
    void markOrdersPaid_shouldUpdateOrderStatusToAssembly() {
        Long orderId = 1L;
        ShopOrder order = ShopOrder.builder()
                .id(orderId)
                .status(ShopOrderStatus.awaiting_payment)
                .build();
        when(shopOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        shopOrderService.markOrdersPaid(List.of(orderId));

        verify(shopOrderRepository).save(argThat(o -> o.getStatus() == ShopOrderStatus.assembly));
    }
}
