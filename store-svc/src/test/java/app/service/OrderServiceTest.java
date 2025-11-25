package app.service;

import app.model.CartItem;
import app.model.Order;
import app.model.OrderItem;
import app.repository.OrderRepository;
import app.service.CartService;
import app.service.OrderService;
import app.web.dto.OrderResponse;
import app.web.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;


    private UUID userId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
    }

    @Test
    void givenCartItems_whenPlaceOrder_thenOrderIsSavedAndCartCleared() {

        CartItem item1 = CartItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .albumId(UUID.randomUUID())
                .albumName("Test album 1")
                .artistName("Test artist 1")
                .price(BigDecimal.valueOf(10))
                .quantity(2)
                .build();

        CartItem item2 = CartItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .albumId(UUID.randomUUID())
                .albumName("Test album 2")
                .artistName("Test artist 2")
                .price(BigDecimal.valueOf(15))
                .quantity(1)
                .build();

        when(cartService.getCart(userId)).thenReturn(List.of(item1, item2));

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdOn(LocalDateTime.now())
                .items(List.of(
                        OrderItem.builder().albumId(item1.getAlbumId()).price(item1.getPrice()).quantity(item1.getQuantity()).build(),
                        OrderItem.builder().albumId(item2.getAlbumId()).price(item2.getPrice()).quantity(item2.getQuantity()).build()
                ))
                .total(BigDecimal.valueOf(35))
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse expectedResponse = OrderMapper.toResponse(savedOrder);

        OrderResponse result = orderService.placeOrder(userId);

        assertNotNull(result);
        assertEquals(expectedResponse.getTotal(), result.getTotal());
        assertEquals(2, result.getItems().size());

        verify(cartService, times(1)).getCart(userId);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartService, times(1)).clearCart(userId);
    }

    @Test
    void givenUserWithOrders_whenGetUserOrders_thenReturnMappedResponses() {

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdOn(LocalDateTime.now())
                .total(BigDecimal.TEN)
                .items(List.of())
                .build();

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order));

        OrderResponse mapped = OrderMapper.toResponse(order);

        List<OrderResponse> result = orderService.getUserOrders(userId);

        assertEquals(1, result.size());
        assertEquals(mapped.getTotal(), result.get(0).getTotal());

        verify(orderRepository, times(1)).findByUserId(userId);
    }

}
