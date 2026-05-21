package com.example.controller.DTO;

import lombok.Data;

@Data
public class CreateAddressRequest {
    private String city;
    private String street;
    private String building;
    private String apartment;
    private String postalCode;
    private Boolean isDefault;
}
