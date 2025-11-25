package app.web;

import app.TestBuilder;
import app.album.model.Album;
import app.album.service.AlbumService;
import app.review.service.ReviewService;
import app.security.AuthenticationDetails;
import app.store.client.dto.OrderResponse;
import app.store.service.StoreService;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreController.class)
public class StoreControllerApiTest {

    @MockitoBean
    private StoreService storeService;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addToCart_shouldReturnViewAlbumWithAttributes() throws Exception {
        UUID albumId = UUID.randomUUID();
        User user = TestBuilder.aRandomUser();
        AuthenticationDetails principal = new AuthenticationDetails(
                user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true
        );
        Album album = TestBuilder.aRandomAlbum(user);

        when(userService.getById(user.getId())).thenReturn(user);
        when(albumService.getById(albumId)).thenReturn(album);
        when(storeService.getCartTotal(user.getId())).thenReturn(BigDecimal.valueOf(100));
        when(reviewService.getReviewsByAlbum(album)).thenReturn(List.of());

        MockHttpServletRequestBuilder request = post("/cart/add/{albumId}", albumId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("view-album"))
                .andExpect(model().attributeExists("album", "user", "newReviewRequest", "reviews", "cartTotal"));

        verify(storeService, times(1)).addToCart(user.getId(), albumId);
    }

    @Test
    void viewCart_shouldReturnCartViewWithAttributes() throws Exception {
        User user = TestBuilder.aRandomUser();
        AuthenticationDetails principal = new AuthenticationDetails(
                user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true
        );

        when(userService.getById(user.getId())).thenReturn(user);
        when(storeService.getCart(user.getId())).thenReturn(List.of());
        when(storeService.getCartTotal(user.getId())).thenReturn(BigDecimal.ZERO);

        MockHttpServletRequestBuilder request = get("/cart").with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("user", "cartItems", "cartTotal"));
    }

    @Test
    void removeFromCart_shouldReturnCartViewWithUpdatedCart() throws Exception {
        UUID albumId = UUID.randomUUID();
        User user = TestBuilder.aRandomUser();
        AuthenticationDetails principal = new AuthenticationDetails(
                user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true
        );

        when(userService.getById(user.getId())).thenReturn(user);
        when(storeService.getCart(user.getId())).thenReturn(List.of());
        when(storeService.getCartTotal(user.getId())).thenReturn(BigDecimal.ZERO);

        MockHttpServletRequestBuilder request = post("/cart/remove/{albumId}", albumId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("user", "cartItems", "cartTotal"));

        verify(storeService, times(1)).removeCartItem(user.getId(), albumId);
    }

    @Test
    void checkout_shouldReturnOrderSuccessView() throws Exception {
        User user = TestBuilder.aRandomUser();
        AuthenticationDetails principal = new AuthenticationDetails(
                user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true
        );

        OrderResponse order = OrderResponse.builder()
                .id(UUID.randomUUID())
                .build();

        when(storeService.placeOrder(user.getId())).thenReturn(order);

        MockHttpServletRequestBuilder request = post("/cart/checkout")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("order-success"))
                .andExpect(model().attributeExists("order"));

        verify(storeService, times(1)).placeOrder(user.getId());
    }

    @Test
    void viewOrders_shouldReturnOrdersHistoryViewWithAttributes() throws Exception {
        User user = TestBuilder.aRandomUser();
        AuthenticationDetails principal = new AuthenticationDetails(
                user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true
        );

        when(userService.getById(user.getId())).thenReturn(user);
        when(storeService.getOrders(user.getId())).thenReturn(List.of());

        MockHttpServletRequestBuilder request = get("/orders").with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("orders-history"))
                .andExpect(model().attributeExists("user", "orders"));
    }
}
