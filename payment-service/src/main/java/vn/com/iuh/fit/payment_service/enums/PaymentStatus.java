package vn.com.iuh.fit.payment_service.enums;

public enum PaymentStatus {
    PENDING,   // Đang xử lý thanh toán
    SUCCESS,   // Thanh toán thành công
    FAILED,    // Thanh toán thất bại
    REFUNDED   // Đã hoàn tiền
}

