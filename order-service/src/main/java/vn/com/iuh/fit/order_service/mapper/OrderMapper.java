package vn.com.iuh.fit.order_service.mapper;

import org.springframework.stereotype.Component;
import vn.com.iuh.fit.order_service.dto.OrderItemResponseDTO;
import vn.com.iuh.fit.order_service.dto.OrderResponseDTO;
import vn.com.iuh.fit.order_service.entity.Order;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponseDTO toDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getImageUrl(),
                        item.getStatus()
                )).toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getTotalPrice(),
                order.getUserId(),
                order.getStatus(),
                items
        );
    }
}
