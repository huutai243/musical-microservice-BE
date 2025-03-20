package vn.com.iuh.fit.order_service.enums;

public enum OrderStatus {
    PENDING_INVENTORY_VALIDATION, // Đang kiểm tra tồn kho
    PENDING_PAYMENT,              // Đang chờ thanh toán
    CONFIRMED,                    // Đã xác nhận đơn hàng
    PARTIALLY_CONFIRMED,          // Một số sản phẩm có sẵn, một số hết hàng
    PENDING_CUSTOMER_ACTION,      // Chờ khách hàng xử lý (ví dụ: chọn sản phẩm thay thế)
    CANCELLED,                    // Đơn hàng bị hủy hoàn toàn
    SHIPPED,                      // Đã gửi hàng
    DELIVERED ,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED
}
