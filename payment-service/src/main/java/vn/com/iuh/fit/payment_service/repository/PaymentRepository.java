package vn.com.iuh.fit.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.payment_service.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
