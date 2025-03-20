package vn.com.iuh.fit.order_service.enums;

public enum OrderItemStatus {
    PENDING,       // Đang chờ xử lý (chưa xác nhận tồn kho)
    CONFIRMED,     // Đã xác nhận sản phẩm có trong kho
    OUT_OF_STOCK,  // Hết hàng, không thể mua
    CANCELLED,     // Sản phẩm bị hủy khỏi đơn hàng
    PARTIALLY_CONFIRMED, // Một phần sản phẩm được xác nhận (đối với đơn hàng có số lượng lớn)
    SHIPPED,       // Sản phẩm đã được giao cho đơn vị vận chuyển
    DELIVERED      // Sản phẩm đã được giao thành công
}
