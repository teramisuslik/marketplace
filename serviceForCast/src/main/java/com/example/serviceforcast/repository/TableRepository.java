package com.example.serviceforcast.repository;

import com.example.serviceforcast.entity.Table;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableRepository extends JpaRepository<Table,Long> {

    List<Table> findAllByUserId(Long userId);
}
