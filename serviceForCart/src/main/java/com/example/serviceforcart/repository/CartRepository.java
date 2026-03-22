package com.example.serviceforcart.repository;

import com.example.serviceforcart.entity.Cart;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findAllByUserId(Long userId);
}
