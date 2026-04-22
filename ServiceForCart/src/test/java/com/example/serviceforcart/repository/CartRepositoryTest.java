package com.example.serviceforcart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.serviceforcart.entity.Cart;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
    }

    @Test
    void findAllByUserId_ShouldReturnAllCartsForGivenUser() {
        // Given
        Cart cart1 = Cart.builder().userId(userId).productId(100L).build();
        Cart cart2 = Cart.builder().userId(userId).productId(200L).build();
        Cart cartOtherUser = Cart.builder().userId(2L).productId(300L).build();

        cartRepository.saveAll(List.of(cart1, cart2, cartOtherUser));

        // When
        List<Cart> found = cartRepository.findAllByUserId(userId);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Cart::getProductId).containsExactly(100L, 200L);
    }

    @Test
    void findAllByUserId_ShouldReturnEmptyList_WhenNoCartsForUser() {
        // Given
        // Нет корзин

        // When
        List<Cart> found = cartRepository.findAllByUserId(userId);

        // Then
        assertThat(found).isEmpty();
    }
}
