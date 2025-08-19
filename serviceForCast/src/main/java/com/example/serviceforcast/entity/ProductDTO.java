package com.example.serviceforcast.entity;

import lombok.Data;

@Data
public class ProductDTO {

    private String name;

    private String description;

    private Integer countOfProduct;

    private Float rating;

    private Long sellerId;
}
