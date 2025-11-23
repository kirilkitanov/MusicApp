package app.service;

import app.model.CartItem;
import app.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {
    private final CartItemRepository cartRepository;

    @Autowired
    public CartService(CartItemRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartItem addItemToCart(CartItem item) {
        return cartRepository.save(item);
    }

    public List<CartItem> getCart(UUID userId) {
        return cartRepository.findByUserId(userId);
    }

    public BigDecimal getCurrentSum(UUID userId) {
        return cartRepository.findByUserId(userId).stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void removeItemByAlbum(UUID userId, UUID albumId) {
        List<CartItem> cart = getCart(userId);
        cart.stream()
                .filter(item -> item.getAlbumId().equals(albumId))
                .findFirst()
                .ifPresent(item -> removeItem(item.getId()));
    }
    public void removeItem(UUID cartItemId) {
        cartRepository.deleteById(cartItemId);
    }

    public void clearCart(UUID userId) {
        cartRepository.deleteByUserId(userId);
    }
}
