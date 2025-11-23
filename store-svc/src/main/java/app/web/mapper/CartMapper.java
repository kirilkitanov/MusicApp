package app.web.mapper;

import app.model.CartItem;
import app.web.dto.AddItemRequest;
import app.web.dto.CartItemResponse;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class CartMapper {

    public static CartItem toCartItem(AddItemRequest request) {
        return CartItem.builder()
                .userId(request.getUserId())
                .albumId(request.getAlbumId())
                .albumName(request.getAlbumName())
                .artistName(request.getArtistName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .addedOn(LocalDateTime.now())
                .build();
    }

    public static CartItemResponse toResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .albumId(item.getAlbumId())
                .albumName(item.getAlbumName())
                .artistName(item.getArtistName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
