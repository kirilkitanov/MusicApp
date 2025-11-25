package app.store.service;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.store.client.StoreClient;
import app.store.client.dto.CartItem;
import app.store.client.dto.OrderResponse;
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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StoreServiceUTest {

    @Mock
    private StoreClient storeClient;

    @Mock
    private AlbumService albumService;

    @InjectMocks
    private StoreService storeService;

    @Test
    void addToCart_shouldCallStoreClientWithCorrectRequest() {
        UUID userId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();
        Album album = Album.builder()
                .id(albumId)
                .albumName("Test Album")
                .artistName("Test Artist")
                .price(BigDecimal.valueOf(20))
                .build();

        when(albumService.getById(albumId)).thenReturn(album);

        storeService.addToCart(userId, albumId);

        verify(storeClient, times(1)).addToCart(argThat(request ->
                request.getUserId().equals(userId) &&
                        request.getAlbumId().equals(albumId) &&
                        request.getAlbumName().equals("Test Album") &&
                        request.getArtistName().equals("Test Artist") &&
                        request.getPrice().equals(BigDecimal.valueOf(20)) &&
                        request.getQuantity() == 1
        ));
    }

    @Test
    void getCartTotal_shouldReturnCartSumFromClient() {
        UUID userId = UUID.randomUUID();
        when(storeClient.getCartSum(userId)).thenReturn(BigDecimal.valueOf(100));

        BigDecimal total = storeService.getCartTotal(userId);

        assertEquals(BigDecimal.valueOf(100), total);
        verify(storeClient, times(1)).getCartSum(userId);
    }

    @Test
    void getCart_shouldReturnCartItemsFromClient() {
        UUID userId = UUID.randomUUID();
        CartItem item = CartItem.builder().albumId(UUID.randomUUID()).albumName("Album").price(BigDecimal.TEN).build();
        when(storeClient.getCart(userId)).thenReturn(List.of(item));

        List<CartItem> cart = storeService.getCart(userId);

        assertEquals(1, cart.size());
        assertEquals("Album", cart.get(0).getAlbumName());
        verify(storeClient, times(1)).getCart(userId);
    }

    @Test
    void removeCartItem_shouldCallStoreClient() {
        UUID userId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();

        storeService.removeCartItem(userId, albumId);

        verify(storeClient, times(1)).removeCartItem(userId, albumId);
    }

    @Test
    void placeOrder_shouldReturnOrderResponseFromClient() {
        UUID userId = UUID.randomUUID();
        OrderResponse order = OrderResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdOn(LocalDateTime.now())
                .total(BigDecimal.valueOf(50))
                .items(List.of())
                .build();

        when(storeClient.placeOrder(userId)).thenReturn(order);

        OrderResponse result = storeService.placeOrder(userId);

        assertEquals(order, result);
        verify(storeClient, times(1)).placeOrder(userId);
    }

    @Test
    void getOrders_shouldReturnSortedOrders() {
        UUID userId = UUID.randomUUID();
        OrderResponse order1 = OrderResponse.builder().id(UUID.randomUUID()).createdOn(LocalDateTime.now().minusDays(1)).build();
        OrderResponse order2 = OrderResponse.builder().id(UUID.randomUUID()).createdOn(LocalDateTime.now()).build();

        when(storeClient.getOrders(userId)).thenReturn(List.of(order1, order2));

        List<OrderResponse> result = storeService.getOrders(userId);

        assertEquals(2, result.size());
        assertEquals(order2, result.get(0));
        assertEquals(order1, result.get(1));
        verify(storeClient, times(1)).getOrders(userId);
    }

}
