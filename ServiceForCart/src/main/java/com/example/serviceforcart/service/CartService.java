package com.example.serviceforcart.service;

import com.example.serviceforcart.client.ProductClient;
import com.example.serviceforcart.client.UserClient;
import com.example.serviceforcart.entity.Cart;
import com.example.serviceforcart.entity.ProductDTO;
import com.example.serviceforcart.repository.CartRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductClient productClient;
    private final UserClient userClient;
    private final CartRepository cartRepository;

    public void addProductToCart(String token, String name) {
        Long userId = userClient.getUserId(token);
        Long ProductId = productClient.findProductByName(name);
        Cart cart = Cart.builder().userId(userId).productId(ProductId).build();

        cartRepository.save(cart);
    }

    public List<ProductDTO> getCart(String token) {
        Long userId = userClient.getUserId(token);
        List<Cart> listCart = cartRepository.findAllByUserId(userId);

        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Cart t : listCart) {
            Long productId = t.getProductId();
            ProductDTO product = productClient.findProductById(productId);
            productDTOList.add(product);
        }

        return productDTOList;
    }
}
