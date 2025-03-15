package vn.com.iuh.fit.payment_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;
import vn.com.iuh.fit.payment_service.entity.Payment;
import vn.com.iuh.fit.payment_service.service.PaymentService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Xử lý thanh toán
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(@RequestBody @Valid PaymentRequestDTO paymentRequest) {
        Payment payment = paymentService.processPayment(paymentRequest);
        return ResponseEntity.ok(payment);
    }

    /**
     * Hoàn tiền giao dịch
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<String> processRefund(@PathVariable Long paymentId) {
        paymentService.processRefund(paymentId);
        return ResponseEntity.ok("Hoàn tiền thành công cho Payment ID: " + paymentId);
    }

    /**
     * Lấy danh sách tất cả các giao dịch thanh toán
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * Lấy chi tiết thanh toán theo ID
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }
}
