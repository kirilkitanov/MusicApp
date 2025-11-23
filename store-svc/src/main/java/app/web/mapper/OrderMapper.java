package app.web.mapper;

import app.model.Order;
import app.model.OrderItem;
import app.web.dto.OrderItemResponse;
import app.web.dto.OrderResponse;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .createdOn(order.getCreatedOn())
                .total(order.getTotal())
                .items(items)
                .build();
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .albumId(item.getAlbumId())
                .albumName(item.getAlbumName())
                .artistName(item.getArtistName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
