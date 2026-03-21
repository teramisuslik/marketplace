package com.example.serviceforcart.repository;

import com.example.serviceforcart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart,Long> {

    List<Cart> findAllByUserId(Long userId);
}
