package app.service;

import app.model.CartItem;
import app.repository.CartItemRepository;
import app.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceUTest {
    @Mock
    private CartItemRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void givenValidItem_whenAddItemToCart_thenRepositorySaveIsCalled() {

        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .albumId(UUID.randomUUID())
                .price(BigDecimal.TEN)
                .quantity(1)
                .build();

        when(cartRepository.save(any(CartItem.class))).thenReturn(item);

        CartItem result = cartService.addItemToCart(item);

        assertNotNull(result);
        verify(cartRepository, times(1)).save(item);
    }

    @Test
    void givenUserId_whenGetCart_thenReturnUserCart() {

        UUID userId = UUID.randomUUID();
        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .albumId(UUID.randomUUID())
                .price(BigDecimal.TEN)
                .quantity(2)
                .build();

        when(cartRepository.findByUserId(userId))
                .thenReturn(List.of(cartItem));

        List<CartItem> result = cartService.getCart(userId);

        assertEquals(1, result.size());
        assertTrue(result.contains(cartItem));
        verify(cartRepository, times(1)).findByUserId(userId);
    }

    @Test
    void givenCartItems_whenGetCurrentSum_thenReturnCorrectTotal() {

        UUID userId = UUID.randomUUID();

        CartItem item1 = CartItem.builder()
                .price(BigDecimal.valueOf(10))
                .quantity(2)
                .build();

        CartItem item2 = CartItem.builder()
                .price(BigDecimal.valueOf(5))
                .quantity(3)
                .build();

        when(cartRepository.findByUserId(userId))
                .thenReturn(List.of(item1, item2));

        BigDecimal total = cartService.getCurrentSum(userId);

        assertEquals(BigDecimal.valueOf(35), total);
    }

    @Test
    void givenAlbumId_whenRemoveItemByAlbum_thenDeleteIsCalled() {

        UUID userId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        CartItem cartItem = CartItem.builder()
                .id(cartItemId)
                .userId(userId)
                .albumId(albumId)
                .build();

        when(cartRepository.findByUserId(userId))
                .thenReturn(List.of(cartItem));

        cartService.removeItemByAlbum(userId, albumId);

        verify(cartRepository, times(1)).deleteById(cartItemId);
    }

    @Test
    void givenCartItemId_whenRemoveItem_thenRepositoryDeleteIsCalled() {

        UUID cartItemId = UUID.randomUUID();

        cartService.removeItem(cartItemId);

        verify(cartRepository, times(1)).deleteById(cartItemId);
    }

    @Test
    void givenUserId_whenClearCart_thenRepositoryDeleteByUserIdIsCalled() {

        UUID userId = UUID.randomUUID();

        cartService.clearCart(userId);

        verify(cartRepository, times(1)).deleteByUserId(userId);
    }

}
