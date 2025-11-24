package app.store.service;


import app.album.model.Album;
import app.album.service.AlbumService;
import app.store.client.StoreClient;
import app.store.client.dto.AddItemRequest;
import app.store.client.dto.CartItem;
import app.store.client.dto.OrderResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class StoreService {

    private final StoreClient storeClient;
    private final AlbumService albumService;

    public StoreService(StoreClient storeClient, AlbumService albumService) {
        this.storeClient = storeClient;
        this.albumService = albumService;
    }
    public void addToCart(UUID userId, UUID albumId) {
        Album album = albumService.getById(albumId);

        AddItemRequest request = AddItemRequest.builder()
                .userId(userId)
                .albumId(album.getId())
                .albumName(album.getAlbumName())
                .artistName(album.getArtistName())
                .price(album.getPrice())
                .quantity(1)
                .build();

        storeClient.addToCart(request);
    }

    public BigDecimal getCartTotal(UUID userId) {
        return storeClient.getCartSum(userId);
    }

    public List<CartItem> getCart(UUID userId) {
        return storeClient.getCart(userId);
    }

    public void removeCartItem(UUID userId, UUID albumId) {
        storeClient.removeCartItem(userId, albumId);
    }

    public OrderResponse placeOrder(UUID userId) {
        return storeClient.placeOrder(userId);
    }

    public List<OrderResponse> getOrders(UUID userId) {
        return storeClient.getOrders(userId)
                .stream()
                .sorted(Comparator.comparing(OrderResponse::getCreatedOn).reversed())
                .toList();
    }
}
