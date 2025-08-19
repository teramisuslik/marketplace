package com.example.serviceforcast.service;

import com.example.serviceforcast.client.ProductClient;
import com.example.serviceforcast.client.UserClient;
import com.example.serviceforcast.entity.ProductDTO;
import com.example.serviceforcast.entity.Table;
import com.example.serviceforcast.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductClient productClient;
    private final UserClient userClient;
    private final TableRepository tableRepository;

    public void addProductToCart(String token, String name){
       Long userId =  userClient.getUserId(token);
       Long ProductId = productClient.findProductByName(name);
       Table table = Table.builder()
               .userId(userId)
               .productId(ProductId)
               .build();

       tableRepository.save(table);
    }

    public List<ProductDTO> getCart(String token){
       Long userId =  userClient.getUserId(token);
       List<Table> liastTable = tableRepository.findAllByUserId(userId);

       List<ProductDTO> productDTOList = new ArrayList<>();
       for (Table t : liastTable) {
           Long productId =  t.getProductId();
           ProductDTO product = productClient.findProductById(productId);
           productDTOList.add(product);
       }

       return productDTOList;
    }

}
