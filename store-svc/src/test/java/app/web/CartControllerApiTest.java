package app.web;

import app.model.CartItem;
import app.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerApiTest {

    @MockitoBean
    private CartService cartService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addItem_shouldReturnCreatedCartItem() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();

        CartItem savedItem = CartItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .albumId(albumId)
                .albumName("Test Album")
                .artistName("Test Artist")
                .price(BigDecimal.valueOf(20))
                .quantity(1)
                .build();

        when(cartService.addItemToCart(any(CartItem.class))).thenReturn(savedItem);

        MockHttpServletRequestBuilder request = post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "userId": "%s",
                            "albumId": "%s",
                            "albumName": "Test Album",
                            "artistName": "Test Artist",
                            "price": 20,
                            "quantity": 1
                        }
                        """.formatted(userId, albumId));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedItem.getId().toString()))
                .andExpect(jsonPath("$.albumName").value("Test Album"))
                .andExpect(jsonPath("$.artistName").value("Test Artist"))
                .andExpect(jsonPath("$.price").value(20))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void getCartSum_shouldReturnTotal() throws Exception {
        UUID userId = UUID.randomUUID();

        when(cartService.getCurrentSum(userId)).thenReturn(BigDecimal.valueOf(50));

        mockMvc.perform(get("/api/cart/sum")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }

    @Test
    void getCart_shouldReturnCartItems() throws Exception {
        UUID userId = UUID.randomUUID();

        CartItem item1 = CartItem.builder()
                .id(UUID.randomUUID())
                .albumName("Album1")
                .artistName("Artist1")
                .price(BigDecimal.valueOf(20))
                .quantity(1)
                .build();

        CartItem item2 = CartItem.builder()
                .id(UUID.randomUUID())
                .albumName("Album2")
                .artistName("Artist2")
                .price(BigDecimal.valueOf(30))
                .quantity(2)
                .build();

        when(cartService.getCart(userId)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/cart")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].albumName").value("Album1"))
                .andExpect(jsonPath("$[1].albumName").value("Album2"));
    }

    @Test
    void removeItem_shouldReturnNoContent() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();

        doNothing().when(cartService).removeItemByAlbum(userId, albumId);

        mockMvc.perform(delete("/api/cart/remove")
                        .param("userId", userId.toString())
                        .param("albumId", albumId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cartService).removeItemByAlbum(userId, albumId);
    }
}
