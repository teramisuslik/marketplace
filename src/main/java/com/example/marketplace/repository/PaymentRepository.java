package com.example.marketplace.repository;

import com.example.marketplace.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByYookassaPaymentId(String yookassaPaymentId);

    Optional<Payment> findByIdAndBuyerUserId(Long id, Long buyerUserId);
}
