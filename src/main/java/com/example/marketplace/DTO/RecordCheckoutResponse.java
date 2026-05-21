package com.example.marketplace.DTO;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class RecordCheckoutResponse {
    private List<Long> orderIds = new ArrayList<>();
    private double totalRub;
}
