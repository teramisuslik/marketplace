package com.example.marketplace.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserAddressDTO {
    private Long id;
    private String city;
    private String street;
    private String building;
    private String apartment;
    private String postalCode;

    @JsonProperty("isDefault")
    private boolean defaultAddress;
}
