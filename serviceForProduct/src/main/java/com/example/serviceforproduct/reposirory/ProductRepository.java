package com.example.serviceforproduct.reposirory;


import com.example.serviceforproduct.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    List<Product> findAll();

    Optional<Product> findByName(String name);

    Optional<Product> findById(Long id);

    @Query(value = "SELECT * FROM products WHERE to_tsvector(name || ' ' || description) @@ to_tsquery(:keyword)",
            nativeQuery = true)
    List<Product> findByWord(@Param("keyword") String keyword);
}

