package app.service;

import app.model.CartItem;
import app.model.Order;
import app.model.OrderItem;
import app.repository.OrderRepository;
import app.web.dto.OrderResponse;
import app.web.mapper.OrderMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    @Transactional
    public OrderResponse placeOrder(UUID userId) {
        List<CartItem> cartItems = cartService.getCart(userId);

        Order order = Order.builder()
                .userId(userId)
                .createdOn(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> OrderItem.builder()
                        .albumId(cartItem.getAlbumId())
                        .albumName(cartItem.getAlbumName())
                        .artistName(cartItem.getArtistName())
                        .price(cartItem.getPrice())
                        .quantity(cartItem.getQuantity())
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = orderItems.stream()
                .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(orderItems);
        order.setTotal(total);

        cartService.clearCart(userId);

        Order savedOrder = orderRepository.save(order);

        return OrderMapper.toResponse(savedOrder);
    }

    public List<OrderResponse> getUserOrders(UUID userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }
}
