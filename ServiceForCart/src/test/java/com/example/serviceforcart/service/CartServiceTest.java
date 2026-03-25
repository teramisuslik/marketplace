package com.example.serviceforcart.service;

import com.example.serviceforcart.client.ProductClient;
import com.example.serviceforcart.client.UserClient;
import com.example.serviceforcart.entity.Cart;
import com.example.serviceforcart.entity.ProductDTO;
import com.example.serviceforcart.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductClient productClient;

    @Mock
    private UserClient userClient;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private final String token = "Bearer test-token";
    private final Long userId = 1L;
    private final String productName = "TestProduct";
    private final Long productId = 100L;

    @Test
    void addProductToCart_ShouldSaveCart_WhenUserAndProductExist() {
        // Given
        when(userClient.getUserId(token)).thenReturn(userId);
        when(productClient.findProductByName(productName)).thenReturn(productId);

        // When
        cartService.addProductToCart(token, productName);

        // Then
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        assertThat(savedCart.getUserId()).isEqualTo(userId);
        assertThat(savedCart.getProductId()).isEqualTo(productId);
    }

    @Test
    void getCart_ShouldReturnListOfProductDTO_WhenUserHasProductsInCart() {
        // Given
        when(userClient.getUserId(token)).thenReturn(userId);

        Cart cart1 = Cart.builder().userId(userId).productId(productId).build();
        Cart cart2 = Cart.builder().userId(userId).productId(200L).build();
        List<Cart> cartList = List.of(cart1, cart2);

        when(cartRepository.findAllByUserId(userId)).thenReturn(cartList);

        ProductDTO productDTO1 = new ProductDTO();
        productDTO1.setName("Product1");
        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setName("Product2");

        when(productClient.findProductById(productId)).thenReturn(productDTO1);
        when(productClient.findProductById(200L)).thenReturn(productDTO2);

        // When
        List<ProductDTO> result = cartService.getCart(token);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(productDTO1, productDTO2);

        verify(userClient).getUserId(token);
        verify(cartRepository).findAllByUserId(userId);
        verify(productClient).findProductById(productId);
        verify(productClient).findProductById(200L);
    }

    @Test
    void getCart_ShouldReturnEmptyList_WhenUserCartIsEmpty() {
        // Given
        when(userClient.getUserId(token)).thenReturn(userId);
        when(cartRepository.findAllByUserId(userId)).thenReturn(List.of());

        // When
        List<ProductDTO> result = cartService.getCart(token);

        // Then
        assertThat(result).isEmpty();
        verify(productClient, never()).findProductById(any());
    }
}