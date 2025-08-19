package com.example.serviceforproduct.service;


import com.example.serviceforproduct.DTO.ProductDTO;
import com.example.serviceforproduct.entity.Product;
import com.example.serviceforproduct.exeption.NotFoundExeption;
import com.example.serviceforproduct.reposirory.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(Product product){
        productRepository.save(product);
    }

    public List<ProductDTO> findAll(){
        List<Product> listProducts =  productRepository.findAll();
        List<ProductDTO> listProductDTOs =  new ArrayList<>();
        for (Product product:listProducts){
            ProductDTO productDTO = new ProductDTO();
            productDTO.setName(product.getName());
            productDTO.setDescription(product.getDescription());
            productDTO.setRating(product.getRating());
            productDTO.setSellerId(product.getSellerId());
            productDTO.setCountOfProduct(product.getCountOfProduct());
            listProductDTOs.add(productDTO);
        }
        return listProductDTOs;
    }

    public List<ProductDTO> findByWord(String word){
        List<Product> listProducts =  productRepository.findByWord(word);
        List<ProductDTO> listProductDTOs =  new ArrayList<>();
        for (Product product:listProducts){
            ProductDTO productDTO = new ProductDTO();
            productDTO.setName(product.getName());
            productDTO.setDescription(product.getDescription());
            productDTO.setRating(product.getRating());
            productDTO.setSellerId(product.getSellerId());
            productDTO.setCountOfProduct(product.getCountOfProduct());
            listProductDTOs.add(productDTO);
        }
        return listProductDTOs;
    }

    public Product findById(Long id){
        return productRepository.findById(id).orElse(null);
    }

    public Product findByName(String name)  {
        return productRepository.findByName(name).orElseThrow(() -> new NotFoundExeption("Product not found"));
    }
}
