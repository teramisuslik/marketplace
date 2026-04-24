package com.example.controller.DTO;

import lombok.Data;

@Data
public class ProductDTO {

    private Long id;

    private String name;

    private String description;

    private Integer countOfProduct;

    private Float rating;

    private Long sellerId;

    private Double price;

    private String imageUrl;
}
