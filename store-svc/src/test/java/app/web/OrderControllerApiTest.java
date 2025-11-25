package app.web;

import app.service.OrderService;
import app.web.dto.OrderItemResponse;
import app.web.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerApiTest {

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void placeOrder_shouldReturnCreatedOrder() throws Exception {
        UUID userId = UUID.randomUUID();

        OrderItemResponse item1 = OrderItemResponse.builder()
                .albumId(UUID.randomUUID())
                .albumName("Album1")
                .artistName("Artist1")
                .price(BigDecimal.valueOf(20))
                .quantity(1)
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdOn(LocalDateTime.now())
                .total(BigDecimal.valueOf(20))
                .items(List.of(item1))
                .build();

        when(orderService.placeOrder(any(UUID.class))).thenReturn(response);

        MockHttpServletRequestBuilder request = post("/api/orders/place")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.total").value(20))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].albumName").value("Album1"));
    }

    @Test
    void getOrders_shouldReturnListOfOrders() throws Exception {
        UUID userId = UUID.randomUUID();

        OrderItemResponse item1 = OrderItemResponse.builder()
                .albumId(UUID.randomUUID())
                .albumName("Album1")
                .artistName("Artist1")
                .price(BigDecimal.valueOf(20))
                .quantity(1)
                .build();

        OrderItemResponse item2 = OrderItemResponse.builder()
                .albumId(UUID.randomUUID())
                .albumName("Album2")
                .artistName("Artist2")
                .price(BigDecimal.valueOf(30))
                .quantity(2)
                .build();

        OrderResponse order1 = OrderResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdOn(LocalDateTime.now())
                .total(BigDecimal.valueOf(20))
                .items(List.of(item1))
                .build();

        OrderResponse order2 = OrderResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdOn(LocalDateTime.now())
                .total(BigDecimal.valueOf(60))
                .items(List.of(item2))
                .build();

        when(orderService.getUserOrders(any(UUID.class))).thenReturn(List.of(order1, order2));

        MockHttpServletRequestBuilder request = get("/api/orders/my-orders")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].items[0].albumName").value("Album1"))
                .andExpect(jsonPath("$[1].items[0].albumName").value("Album2"));
    }
}
